package org.bailey.newsreader.channels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.bailey.newsreader.R;
import org.bailey.newsreader.ui.activity.MainActivity;
import org.bailey.scheme.EvaluatorKt;
import org.bailey.scheme.ExpansionKt;
import org.bailey.scheme.LexerKt;
import org.bailey.scheme.ParserKt;
import org.bailey.scheme.ScmEnv;
import org.bailey.scheme.ScmExp;

import java.util.Arrays;
import java.util.List;

public class LispChannel {
    public String name;
    public String script;

    public ScmExp lastResult;
    public LispPost[] posts;
    public Boolean isPosts = false;
    public boolean staleAST = true;
    public Thread evalThread;
    public Runnable onEvalEnd;
    private List<ScmExp> optParsed;

    public LispChannel(String name, String script) {
        this.name = name;
        this.script = script;
    }

    // Posts format:
    // (
    //   ("header 1", "text 1"),
    //   ("header 2", "text 2"),
    //   ("header 3", "text 3"),
    //   ...
    // )
    private static boolean determineIsPosts(ScmExp expression) {
        return (expression instanceof ScmExp.List &&
                (((ScmExp.List) expression).stream()
                        .allMatch(el -> el instanceof ScmExp.List &&
                                ((ScmExp.List) el).size() == 2)));
    }

    public void setScript(String script) {
        this.script = script;
        staleAST = true;
    }

    private List<ScmExp> getAST() {
        if (staleAST) {
            List<ScmExp> ast;
            try {
                ast = ExpansionKt.expandAll(ParserKt.parse(LexerKt.tokenize(script).iterator()));
            } catch (Exception e) {
                LispLogger.logError(e);
                ast = new ScmExp.List(
                        new ScmExp.String(
                                String.format(
                                        "Syntax error!\n%s\n%s\n%s",
                                        e.getMessage(),
                                        e.getCause(),
                                        Arrays.toString(e.getStackTrace())
                                )
                        )
                );
            }
            optParsed = ast;
            staleAST = false;
            return ast;
        } else {
            return optParsed;
        }
    }

    public void startEval() {
        prepareThread();
        evalThread.start();
    }

    public void prepareThread() {
        if (evalThread != null)
            evalThread.interrupt();
        evalThread = new Thread(() -> {
            try {
                ScmEnv env = new ScmEnv(null);
                env.addPrimitives();
                lastResult = EvaluatorKt.evalAll(getAST(), env);
                isPosts = determineIsPosts(lastResult);
                if (isPosts)
                    posts = toPosts((ScmExp.List) lastResult);
            } catch (Exception e) {
                LispLogger.logError(e);
                isPosts = false;
                lastResult = new ScmExp.String(MainActivity.appContext.getString(R.string.error_msg));
            }
            Log.d("LISP EVAL RESULT", String.valueOf(lastResult));

            if (onEvalEnd != null)
                new Handler(Looper.getMainLooper()).post(onEvalEnd);
        });
    }

    @NonNull
    private LispPost[] toPosts(@NonNull ScmExp.List list) {
        int nPosts = list.size();

        LispPost[] ret = new LispPost[nPosts];

        for (int i = 0; i < nPosts; i++) {
            ScmExp.List lst = (ScmExp.List) list.get(i);
            ret[i] = new LispPost(lst.get(0).toString(),
                    lst.get(1).toString());
        }

        return ret;
    }
}

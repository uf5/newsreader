package org.bailey.newsreader.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispChannel;
import org.bailey.newsreader.channels.UserChannels;
import org.bailey.newsreader.ui.dialog.HelpDialog;

// MainActivity - класс Activity отображающий каналы и посты. Является основным в приложении.
public class MainActivity extends AppCompatActivity {
    public static final String APP_PREFERENCES_HELP = "help_show";

    public static MainActivity instance;


    public static Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (appContext == null)
            appContext = getApplicationContext();

        instance = this;

        SharedPreferences sPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main);

        if (!sPreferences.getBoolean(APP_PREFERENCES_HELP, false)) {
            // add example scripts if ran for the first time
            new HelpDialog().show(getSupportFragmentManager(), "HelpDialog");
            UserChannels.add(
                    new LispChannel(
                            "Recursion Demo",
                            "(define (fac n)\n(if (= n 0)\n1\n(* n (fac (- n 1)))))\n(fac 10)"
                    )
            );
            UserChannels.add(
                    new LispChannel(
                            "Closure Demo",
                            "(define (make-counter n)\n(lambda () (set! n (+ n 1)) n))\n(define f (make-counter 0))\n(log (to-string (f)))\n(log (to-string (f)))\n(log (to-string (f)))\n\"Check log to see the result\""
                    )
            );
            UserChannels.add(
                    new LispChannel(
                            "RSS Demo",
                            "(defmacro (find-xml-tag str tag)\n" +
                                    " (define tag1 (string-append \"<\" tag \">\"))\n" +
                                    " (define tag2 (string-append \"</\" tag \">\"))\n" +
                                    " `(map cadr (regex-find-groups\n" +
                                    "   ,str\n" +
                                    "   ,(string-append\n" +
                                    "     tag1\n" +
                                    "     \"([\\\\s\\\\S]*?)\"\n" +
                                    "     tag2))))\n" +
                                    "\n" +
                                    "(define feed-url \"https://www.youtube.com/feeds/videos.xml?channel_id=UCCPlzrYmF2rE34KHcDdql6A\")\n" +
                                    "\n" +
                                    "(define feed (request \"GET\" feed-url))\n" +
                                    "\n" +
                                    "(zip (find-xml-tag feed \"media:title\")\n" +
                                    "     (find-xml-tag feed \"media:description\"))"
                    )
            );
            UserChannels.add(
                    new LispChannel(
                            "Bauman Demo",
                            "(define url \"https://bmstu.ru/news\")\n" +
                                    "\n" +
                                    "(map cdr\n" +
                                    " (regex-find-groups\n" +
                                    " (request \"GET\" url)\n" +
                                    " \"\\\"title\\\":\\\"((?:\\\\\\\\.|[^\\\\\\\\\\\"])*?)\\\",\\\"preview_text\\\":\\\"((?:\\\\\\\\.|[^\\\\\\\\\\\"])*?)\\\",\\\"published_at\\\"\"))"
                    )
            );
            sPreferences.edit().putBoolean(APP_PREFERENCES_HELP, true).apply();
        }

        NavigationUI.setupActionBarWithNavController(this, Navigation.findNavController(this, R.id.main_nav_host_fragment));
    }

    @Override
    public boolean onSupportNavigateUp() {
        Navigation.findNavController(this, R.id.main_nav_host_fragment).navigateUp();
        return super.onSupportNavigateUp();
    }
}
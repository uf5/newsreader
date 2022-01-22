package org.bailey.newsreader.channels;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import kotlin.Pair;

// LispLogger - класс используемый для отладки пользовательских скриптов.
// Поле contents содержит лог-сообщения выполнения скриптов.
public class LispLogger {
    @NonNull
    public static ArrayList<Pair<LogTag, String>> contents = new ArrayList<>();

    public static void log(@NonNull LogTag tag, String msg) {
        android.util.Log.println(tag.ordinal() + 3, "LISP LOG", msg);
        contents.add(new Pair<>(tag, msg));
    }

    public static void logError(@NonNull Exception e) {
        log(LogTag.ERROR, e.getMessage() + Arrays.toString(Arrays.copyOfRange(e.getStackTrace(), 0, 5)));
    }

    public static void clear() {
        contents.clear();
    }

    public enum LogTag {
        DEBUG,
        PRINT,
        WARN,
        ERROR,
    }
}

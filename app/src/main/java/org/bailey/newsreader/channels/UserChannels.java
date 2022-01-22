package org.bailey.newsreader.channels;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import org.bailey.newsreader.ui.activity.MainActivity;

import java.util.ArrayList;

// UserChannels - класс содержащий пользовательский скрипты и сохраняющий пользовательсие скрипты в
// локальную датабазу при помощи SQL.
public class UserChannels {
    private static final DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.appContext);
    private static final ArrayList<LispChannel> channels = dbHelper.getChannels();

    public static LispChannel selectedChannel;

    public static LispChannel get(int n) {
        return channels.get(n);
    }

    public static Integer size() {
        return channels.size();
    }

    public static void add(@NonNull LispChannel channel) {
        dbHelper.addChannel(channel);
        channels.add(channel);
    }

    public static void update(int n) {
        dbHelper.updateChannel(n);
    }

    public static void insert(LispChannel channel, int n) {
        channels.add(n, channel);
        dbHelper.duplicateChannel(n);
    }

    public static void remove(int n) {
        channels.remove(n);
        dbHelper.removeChannel(n);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String TABLE_NAME = "script_table";
        private static final String COL1 = "num";
        private static final String COL2 = "name";
        private static final String COL3 = "script";

        public DatabaseHelper(Context context) {
            super(context, TABLE_NAME, null, 1);
        }

        @Override
        public void onCreate(@NonNull SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL1 + " INTEGER, " + COL2 + " TEXT, " + COL3 + " TEXT)");
        }

        @Override
        public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        @NonNull
        public ArrayList<LispChannel> getChannels() {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            ArrayList<LispChannel> channels = new ArrayList<>();
            while (data.moveToNext())
                channels.add(new LispChannel(data.getString(data.getColumnIndex(COL2)),
                        data.getString(data.getColumnIndex(COL3))));
            data.close();
            return channels;
        }

        @NonNull
        private ContentValues generateValues4Channel(@NonNull LispChannel channel, int n) {
            ContentValues values = new ContentValues();
            values.put(COL1, n);
            values.put(COL2, channel.name);
            values.put(COL3, channel.script);
            return values;
        }

        public void updateChannel(int n) {
            getWritableDatabase().update(TABLE_NAME, generateValues4Channel(UserChannels.get(n), n), COL1 + "=" + n, null);
        }

        private void updateRange(int start, int end) {
            for (int i = start; i < end; i++) {
                updateChannel(i);
            }
        }

        // Add to the end of the database
        public void addChannel(@NonNull LispChannel channel) {
            getWritableDatabase().insert(TABLE_NAME, null, generateValues4Channel(channel, channels.size()));
        }

        // Duplicate channel by specified id
        public void duplicateChannel(int n) {
            updateRange(n, channels.size());
            getWritableDatabase().insert(TABLE_NAME,
                    null,
                    generateValues4Channel(channels.get(channels.size() - 1),
                            channels.size() - 1));
        }

        public void removeChannel(int n) {
            updateRange(n, channels.size());
            getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL1 + "=" + (channels.size()));
        }
    }
}

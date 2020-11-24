package com.fidanoglu.malafatus;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Session.class}, version = 1, exportSchema = false)
public abstract class SessionsDatabase extends RoomDatabase {
    private static SessionsDatabase db;

    public abstract SessionDao sessionDao();

    public static SessionsDatabase getAppDatabase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context.getApplicationContext(), SessionsDatabase.class, "session-db").allowMainThreadQueries().build();
        }
        return db;
    }

    public static void destroyInstance() {
        db = null;
    }
}
package com.moviting.android.util;

import com.google.firebase.database.FirebaseDatabase;

public class DatabaseHelper {
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getInstance() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            //mDatabase.setPersistenceEnabled(true);
        }

        return mDatabase;
    }
}
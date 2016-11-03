package com.moviting.android.util;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by jongseonglee on 9/28/16.
 */

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
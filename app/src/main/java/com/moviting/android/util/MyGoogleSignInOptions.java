package com.moviting.android.util;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class MyGoogleSignInOptions {

    private static final String webClientID = "38252217712-3al861onigtdf6ghmqups6i1o5f588e0.apps.googleusercontent.com";
    private static MyGoogleSignInOptions ourInstance = new MyGoogleSignInOptions();
    private static GoogleSignInOptions mGso;

    public static GoogleSignInOptions getGSO() {
        return ourInstance.mGso;
    }

    private MyGoogleSignInOptions() {
        mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientID)
                .requestEmail()
                .build();
    }
}

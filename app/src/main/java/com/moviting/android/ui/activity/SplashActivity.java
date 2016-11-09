package com.moviting.android.ui.activity;

import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.User;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import io.fabric.sdk.android.Fabric;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mIsAuthed;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        // TODO: Move this to where you establish a user session
        logUser();

        setContentView(R.layout.activity_splash);
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (getFirebaseAuth().getCurrentUser() == null) {
            Handler hd = new Handler();
            hd.postDelayed(new splashhandler(), 1000);
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser onAuthedUser = firebaseAuth.getCurrentUser();
                if (onAuthedUser != null && onAuthedUser == getFirebaseAuth().getCurrentUser() && !mIsAuthed) {
                    // Myself
                    Log.d(TAG, "onAuthStateChanged:signed_in:myself" + onAuthedUser.getUid());
                    mIsAuthed = true;

                    getFirebaseDatabaseReference().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            try{ Thread.sleep(500);}catch(Exception ignored){}

                            if(user == null) {
                                startActivity(LoginActivity.createIntent(SplashActivity.this));
                            } else if(!user.isUserFormFilled()) {
                                startActivity(FirstSettingActivity.createIntent(SplashActivity.this, user));
                            } else {
                                if (!user.token.equals(getToken())) {
                                    getFirebaseDatabaseReference().child("users").child(getUid()).child("token").setValue(getToken());
                                }
                                startActivity(MainActivity.createIntent(SplashActivity.this));
                            }
                            SplashActivity.this.finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });

                } else if(onAuthedUser != null) {
                    // A user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + onAuthedUser.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    private class splashhandler implements Runnable{
        public void run() {
            startActivity(LoginActivity.createIntent(SplashActivity.this));
            SplashActivity.this.finish();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        getFirebaseAuth().addAuthStateListener(mAuthListener);
        mIsAuthed = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            getFirebaseAuth().removeAuthStateListener(mAuthListener);
        }
    }

    private void logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier("12345");
        Crashlytics.setUserEmail("dazzyjong@gmail.com");
        Crashlytics.setUserName("Test User");
    }

}
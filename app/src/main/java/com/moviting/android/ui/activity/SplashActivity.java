package com.moviting.android.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.BuildConfig;
import com.moviting.android.R;
import com.moviting.android.model.User;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private static final int SERVER_MAINTENANCE = 0;
    private static final int OUT_VERSION = 1;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mIsAuthed;
    private SharedPreferences prefs = null;
    private boolean isFirstRun = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        logUser();

        setContentView(R.layout.activity_splash);

        prefs = getSharedPreferences("com.moviting.android", MODE_PRIVATE);

        isFirstRun = prefs.getBoolean("firstrun", true);

        FacebookSdk.sdkInitialize(getApplicationContext());

        getFirebaseDatabaseReference().child("server_status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> object = (HashMap)dataSnapshot.getValue();
                Boolean maintenance = (Boolean)object.get("maintenance");
                Long minVersion = (Long) object.get("min_version");
                Long time = (Long) object.get("time");
                if(maintenance) {
                    createWarningDialog("서버가 점검중입니다.\n" + time + "시간 이후에 다시 접속해주세요.", SERVER_MAINTENANCE);
                } else if( BuildConfig.VERSION_CODE < minVersion) {
                    createWarningDialog("업데이트가 있습니다.\n앱을 업데이트 해주세요.", OUT_VERSION);
                }else {
                    if (getFirebaseAuth().getCurrentUser() == null) {
                        Handler hd = new Handler();
                        hd.postDelayed(new splashhandler(), 300);
                    }
                    getFirebaseAuth().addAuthStateListener(mAuthListener);
                    mIsAuthed = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });


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

                            if(user == null) {
                                startActivity(LoginActivity.createIntent(SplashActivity.this));
                            } else if(!user.isUserFormFilled()) {
                                startActivity(FirstSettingActivity.createIntent(SplashActivity.this, user));
                            } else {
                                if (user.token == null || !user.token.equals(getToken())) {
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

    private void createWarningDialog(String message, final int mode) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(mode == SERVER_MAINTENANCE) {
                                    finish();
                                } else if(mode == OUT_VERSION) {
                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        finish();
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        finish();
                                    }
                                }
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    private class splashhandler implements Runnable{
        public void run() {
            if(isFirstRun) {
                startActivity(LandingActivity.createIntent(SplashActivity.this));
            } else {
                startActivity(LoginActivity.createIntent(SplashActivity.this));
            }
            SplashActivity.this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            getFirebaseAuth().removeAuthStateListener(mAuthListener);
        }
    }

    private void logUser() {
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier("12345");
        Crashlytics.setUserEmail("dazzyjong@gmail.com");
        Crashlytics.setUserName("Test User");
    }

}
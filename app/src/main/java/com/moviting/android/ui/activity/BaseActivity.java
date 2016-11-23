package com.moviting.android.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.moviting.android.BuildConfig;
import com.moviting.android.R;

import com.google.firebase.iid.FirebaseInstanceId;
import com.moviting.android.util.DatabaseHelper;

import java.util.HashMap;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    private static final int SERVER_MAINTENANCE = 0;
    private static final int OUT_VERSION = 1;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w("BaseActivity::onResume", databaseError.getDetails());
            }
        });
    }

    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    public DatabaseReference getFirebaseDatabaseReference() {
        return DatabaseHelper.getInstance().getReference();
    }

    public FirebaseStorage getFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    private void createWarningDialog(String message, final int mode) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BaseActivity.this);

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
}

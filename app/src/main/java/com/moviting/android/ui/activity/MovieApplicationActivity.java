package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;
import com.moviting.android.model.User;

public class MovieApplicationActivity extends BaseActivity {

    private AgeListner listener;
    private static final String TAG = "MovieApplication";
    private int minAge = 20;
    private int maxAge = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_application);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RangeBar rangeBar = (RangeBar) findViewById(R.id.rangebar);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                Log.d(TAG, "onRangeChangeListener: " + leftPinValue + " / " + rightPinValue);
                minAge = Integer.valueOf(leftPinValue);
                maxAge = Integer.valueOf(rightPinValue);
            }
        });

        Button applicationButton = (Button) findViewById(R.id.application_button);
        applicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                DatabaseReference ref = getFirebaseDatabaseReference().child("users").child(getUid());
                ref.child("minPrefAge").setValue(minAge).addOnCompleteListener(MovieApplicationActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MovieApplicationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        } else {
                            User.getUserInstance().setMinPrefAge(minAge);
                            listener.setAgeFlagAndUpdateUserStatus();
                        }
                    }
                });
                ref.child("maxPrefAge").setValue(maxAge).addOnCompleteListener(MovieApplicationActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MovieApplicationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        } else {
                            User.getUserInstance().setMaxPrefAge(maxAge);
                            listener.setAgeFlagAndUpdateUserStatus();
                        }
                    }
                });
            }
        });

        listener = new AgeListner() {
            int flag = 0;
            @Override
            public void setAgeFlagAndUpdateUserStatus() {
                flag++;

                if(flag == 2){
                    DatabaseReference ref = getFirebaseDatabaseReference().child("users").child(getUid());
                    ref.child("userStatus").setValue("Enrolled").addOnCompleteListener(MovieApplicationActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MovieApplicationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                hideProgressDialog();
                            } else {
                                User.getUserInstance().setUserStatus("Enrolled");
                                hideProgressDialog();
                                Toast.makeText(MovieApplicationActivity.this, R.string.apply_success_text, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                MovieApplicationActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, MovieApplicationActivity.class);
        return in;
    }

    public interface AgeListner {
        public void setAgeFlagAndUpdateUserStatus();
    }
}

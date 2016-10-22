package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;

public class ProfilePropEditActivity extends BaseActivity {

    private TextInputEditText input;
    private String key;
    private String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_prop_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        input = (TextInputEditText) findViewById(R.id.input);

        key = getIntent().getStringExtra("key");
        value = getIntent().getStringExtra("value");

        setActionBarTitle(key);
        setInputEditFormat(key);
        input.setText(value);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ProfilePropEditActivity.this.onBackPressed();
                break;
            case R.id.action_complete:
                    if(input.length() != 0) {

                        if(getPropertyName(key).equals("myAge")) {
                            getFirebaseDatabaseReference().child("users").child(getUid()).child(getPropertyName(key)).setValue(Long.valueOf(input.getText().toString()), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Intent intent = new Intent();
                                    intent.putExtra("key", key);
                                    intent.putExtra("value", input.getText().toString());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                        } else {
                            getFirebaseDatabaseReference().child("users").child(getUid()).child(getPropertyName(key)).setValue(input.getText().toString(), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Intent intent = new Intent();
                                    intent.putExtra("key", key);
                                    intent.putExtra("value", input.getText().toString());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                        }
                    }
                break;
        }
        return true;
    }

    public static Intent createIntent(Context context, String key, String value) {
        Intent in = new Intent();
        in.putExtra("key", key);
        in.putExtra("value", value);
        in.setClass(context, ProfilePropEditActivity.class);
        return in;
    }

    private void setActionBarTitle(String key) {
        getSupportActionBar().setTitle(key);
    }

    private void setInputEditFormat(String key) {
        if(getPropertyName(key).equals("myAge") || getPropertyName(key).equals("height")){
            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL  | InputType.TYPE_CLASS_NUMBER);
        }
    }

    private String getPropertyName(String key) {
        if(key.equals("이름")) {
            return "name";
        }
        if(key.equals("나이")) {
            return "myAge";
        }
        if(key.equals("키")) {
            return "height";
        }
        if(key.equals("학교")) {
            return "school";
        }
        if(key.equals("직업")) {
            return "work";
        }
        if(key.equals("인생 영화")) {
            return "favoriteMovie";
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_pref, menu);
        return true;
    }
}

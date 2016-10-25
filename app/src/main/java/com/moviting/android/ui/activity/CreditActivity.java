package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;

public class CreditActivity extends BaseActivity {

    private static final String TAG = "CreditActivity";
    private TextView availableCredit;
    private Long credit = 0L;
    private CheckBox checkBox;
    private EditText userUseAmount;
    private Long userAmount = 0L;
    private Button useButton;
    private TextView usedCredit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usedCredit = (TextView) findViewById(R.id.used_credit);
        availableCredit = (TextView) findViewById(R.id.available_credit);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    userUseAmount.setText(String.valueOf(credit));
                    userAmount = credit;
                    usedCredit.setText(availableCredit.getText());
                } else {
                    userUseAmount.setText("0");
                    userAmount = 0L;
                    usedCredit.setText("0");
                }
            }
        });

        userUseAmount = (EditText) findViewById(R.id.user_input);
        userUseAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().trim().length() > 0 ) {

                    if( Long.parseLong(charSequence.toString()) > credit || charSequence.toString().trim().length() > availableCredit.length()) {
                        userUseAmount.setText(availableCredit.getText());
                        userAmount = credit;
                        usedCredit.setText(availableCredit.getText());
                    } else {
                        userAmount = Long.parseLong(charSequence.toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        useButton = (Button) findViewById(R.id.use_button);
        useButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    Intent intent = new Intent();
                    intent.putExtra("usedCredit", userUseAmount.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        getAvailableCredit();
    }

    private boolean validate(){

        if(credit == 0) {
            Toast.makeText(this, "사용가능 한 크레딧이 없습니다", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(userAmount < 0) {
            Toast.makeText(this, "사용하실 크레딧을 입력해 주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void getAvailableCredit(){
        getFirebaseDatabaseReference().child("user_point").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    availableCredit.setText(String.format("%,d", dataSnapshot.getValue() ));
                    if ((Long) dataSnapshot.getValue() >= 0) {
                        credit = (Long) dataSnapshot.getValue();
                    }
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
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, CreditActivity.class);
        return in;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                CreditActivity.this.onBackPressed();
                break;
        }
        return true;
    }
}

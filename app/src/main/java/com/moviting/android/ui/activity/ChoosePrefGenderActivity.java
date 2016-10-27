package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ChoosePrefGenderActivity extends ChoosePrefActivity {
    private static final String TAG = "PrefGender";
    private String choosedGender;
    private ArrayList<String> options = new ArrayList<>(Arrays.asList("male", "female"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPrefOptionList().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        choosedGender = getIntent().getStringExtra("preferredGender");

        getUserPrefGender();

        getPrefOptionList().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                choosedGender = options.get(i);
            }
        });
    }

    public void getUserPrefGender() {
        String[] list = new String[]{"남성", "여성"};
        boolean[] select = {false, false, false};

        if(choosedGender.length() != 0) {
            select[options.indexOf(choosedGender)] = true;
            createAdapterAndAddItems(getPrefOptionList(), list, select);
        } else {
            createAdapterAndAddItems(getPrefOptionList(), list, select);
        }
    }

    public void updateSetting()  {
        showProgressDialog();
        getFirebaseDatabaseReference().child("users").child(getUid()).child("preferredGender").setValue(choosedGender, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressDialog();
                Intent intent = new Intent();
                intent.putExtra("preferredGender", choosedGender);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public static Intent createIntent(Context context, String preferredGender) {
        Intent in = new Intent();
        in.putExtra("preferredGender", preferredGender);
        in.setClass(context, ChoosePrefGenderActivity.class);
        return in;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ChoosePrefGenderActivity.this.onBackPressed();
                break;
            case R.id.action_complete:
                if(choosedGender.length() != 0) {
                    updateSetting();
                } else {
                    Toast.makeText(this, "성별을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }
}

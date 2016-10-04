package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jongseonglee on 9/27/16.
 */

public class ChoosePrefDateActivity extends BaseActivity {

    private static final String TAG = "PrefDate";
    private MaterialCalendarView calendarView;
    private ArrayList<String> selectedDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pref_date);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectedDates = getIntent().getStringArrayListExtra("preferredDate");

        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formatted = sdFormat.format(date.getDate());
                 if(selected) {
                     selectedDates.add(formatted);

                 } else {
                     selectedDates.remove(formatted);
                 }
                Log.d(TAG, formatted);
            }
        });
        getUserPrefDate();
    }

    public void getUserPrefDate() {
        DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        for( String select : selectedDates){
            try {
                Date date =sdFormat.parse(select);
                calendarView.setDateSelected(date, true);
                Log.d(TAG, select);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSetting() {
        showProgressDialog();
        getFirebaseDatabaseReference().child("users").child(getUid()).child("preferredDate").setValue(selectedDates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressDialog();
                Intent intent = new Intent();
                intent.putStringArrayListExtra("preferredDate", selectedDates);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ChoosePrefDateActivity.this.onBackPressed();
                break;
            case R.id.action_complete:
                if(selectedDates.size() != 0) {
                    updateSetting();
                } else {
                    Toast.makeText(this, "날짜를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    public static Intent createIntent(Context context, ArrayList<String> preferredDate) {
        Intent in = new Intent();
        in.putStringArrayListExtra("preferredDate", preferredDate);
        in.setClass(context, ChoosePrefDateActivity.class);
        return in;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_pref, menu);
        return true;
    }
}

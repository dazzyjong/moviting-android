package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ChoosePrefMovieActivity extends ChoosePrefActivity {

    private static final String TAG = "PrefMovie";
    private ArrayList<String> selectedMovies;
    private ArrayList<String> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedMovies = new ArrayList<>();
        options = new ArrayList<>();

        selectedMovies = getIntent().getStringArrayListExtra("preferredMovie");

        getPrefOptionList().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getPrefOptionList().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(getPrefOptionList().isItemChecked(i)) {
                    selectedMovies.add(options.get(i));
                } else {
                    selectedMovies.remove(options.get(i));
                }
            }
        });
        getMovieOptionList();
    }

    public void getMovieOptionList() {
        getFirebaseDatabaseReference().child("movie").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                options = (ArrayList<String>)dataSnapshot.getValue();
                getUserPrefMovie();
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

    public void getUserPrefMovie() {
        String[] option_list =  options.toArray(new String[0]);
        boolean[] selected_check = new boolean[option_list.length];

        Arrays.fill(selected_check, false);

        for( String select : selectedMovies){
            int index = options.indexOf(select);
            if( index != -1) {
                selected_check[index] = true;
            }
        }
        createAdapterAndAddItems(getPrefOptionList(), option_list, selected_check);
    }

    private void updateSetting() {
        showProgressDialog();
        getFirebaseDatabaseReference().child("users").child(getUid()).child("preferredMovie").setValue(selectedMovies, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressDialog();
                Intent intent = new Intent();
                intent.putStringArrayListExtra("preferredMovie", selectedMovies);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public static Intent createIntent(Context context, ArrayList<String> preferredMovie) {
        Intent in = new Intent();
        in.putStringArrayListExtra("preferredMovie", preferredMovie);
        in.setClass(context, ChoosePrefMovieActivity.class);
        return in;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ChoosePrefMovieActivity.this.onBackPressed();
                break;
            case R.id.action_complete:
                if(selectedMovies.size() !=0) {
                    updateSetting();
                } else {
                    Toast.makeText(this, "영화를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_pref, menu);
        return true;
    }
}

package com.moviting.android.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.moviting.android.R;

public class ChoosePrefActivity extends BaseActivity {

    private ListView mPrefOptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_preference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefOptionList = (ListView) findViewById(R.id.pref_option_list);
    }

    public ListView getPrefOptionList() {
        return mPrefOptionList;
    }

    public void createAdapterAndAddItems(ListView view, String[] values, boolean[] user_select) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.choose_preference_item, R.id.item_text, values);
        view.setAdapter(adapter);

        for(int i = 0; i < user_select.length ; i++) {
            view.setItemChecked(i, user_select[i]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_pref, menu);
        return true;
    }
}

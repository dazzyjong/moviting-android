package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.moviting.android.R;
import com.moviting.android.ui.fragment.ProfileFragment;

public class ProfileActivity extends BaseActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFragment = new ProfileFragment();
        nextFragment(mFragment, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ProfileActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    public void nextFragment(Fragment fragment, boolean isFirst) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.profile_fragment, fragment, fragment.getClass().getSimpleName());
        if(!isFirst) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }

        fragmentTransaction.commit();
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, ProfileActivity.class);
        return in;
    }
}

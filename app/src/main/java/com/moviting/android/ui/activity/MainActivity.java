package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.analytics.FirebaseAnalytics;

import com.moviting.android.R;
import com.moviting.android.ui.fragment.AccountFragment;
import com.moviting.android.ui.fragment.MatchFragment;
import com.moviting.android.ui.fragment.ProposeFragment;
import com.moviting.android.ui.fragment.EnrollFragment;
import com.moviting.android.util.MyViewPager;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private MyViewPager mViewPager;
    private SharedPreferences prefs;
    private TabLayout tabLayout;
    private int res[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        prefs = getSharedPreferences("tab_number", MODE_PRIVATE);
        int tabNum = prefs.getInt("tab", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (MyViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);
        res = new int[]{R.drawable.ic_av_movie_grey,
                R.drawable.ic_action_favorite_grey,
                R.drawable.ic_chat_bubble_grey_24dp,
                R.drawable.ic_navigation_more_horiz_grey};
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(res[i]);
        }

        TabLayout.Tab tab = tabLayout.getTabAt(tabNum);
        tab.select();

        Bundle params = new Bundle();
        params.putString("name", "MainActivity");
        params.putString("value", "onCreate");
        mFirebaseAnalytics.logEvent("log", params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("tab", tabLayout.getSelectedTabPosition());
        ed.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", "com.moviting.android", null);
            intent.setData(uri);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EnrollFragment.newInstance();
                case 1:
                    return ProposeFragment.newInstance();
                case 2:
                    return MatchFragment.newInstance();
                case 3:
                    return AccountFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, MainActivity.class);
        return in;
    }
}

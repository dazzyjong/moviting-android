package com.moviting.android.ui.fragment;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;

public class ProposeFragment extends Fragment {

    private static final String TAG = "ProposeFragment";
    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    public ProposeFragment() {
        // Required empty public constructor
    }

    public static ProposeFragment newInstance() {
        ProposeFragment fragment = new ProposeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPagerAdapter = new ProposePageAdapter(getFragmentManager());
        //DatabaseReference ref = getActivity().getFirebaseDatabase().getReference().child("users").child(getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_propose, container, false);
        Log.d(TAG, String.valueOf(this));
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }

    private class ProposePageAdapter extends FragmentStatePagerAdapter {
        public ProposePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, " / " + position);
            return ProposePageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}

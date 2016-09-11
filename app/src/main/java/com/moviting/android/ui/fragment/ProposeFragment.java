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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.Propose;

import java.util.ArrayList;
import java.util.HashMap;

public class ProposeFragment extends BaseFragment {

    private static final String TAG = "ProposeFragment";
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private ArrayList<Propose> mProposeList;
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
        Log.d(TAG, "onCreate");
        mProposeList = new ArrayList<>();
        getProposeList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_propose, container, false);
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new ProposePageAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        return rootView;
    }

    public void getProposeList(){
        DatabaseReference ref = getBaseActivity().getFirebaseDatabaseReference().child("propose").child(getBaseActivity().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    String proposeStatus = ((HashMap<String, String>) child.getValue()).get("status");
                    if(!proposeStatus.equals("Dislike")) {
                        Propose propose = new Propose();
                        propose.setUid(child.getKey());
                        propose.setStatus(proposeStatus);
                        mProposeList.add(propose);
                    }
                }
                if(mPagerAdapter != null ) {
                    mPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getProposeList:onCancelled", databaseError.toException());
            }
        });
    }

    private class ProposePageAdapter extends FragmentStatePagerAdapter {
        public ProposePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem / " + position);
            ProposePageFragment proposePageFragment = ProposePageFragment.newInstance(mProposeList.get(position).getUid(), mProposeList.get(position).getStatus());
            proposePageFragment.page = position;

            return proposePageFragment;
        }

        @Override
        public int getCount() {
            Log.d(TAG, "getCount / " + mProposeList.size());
            return mProposeList.size();
        }

        @Override
        public int getItemPosition(Object item) {
            ProposePageFragment fragment = (ProposePageFragment)item;
            Log.d(TAG, "getItemPosition / " + fragment.page);
            if (fragment.page >= 0) {
                return fragment.page;
            } else {
                return POSITION_NONE;
            }
        }
    }
}

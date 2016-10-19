package com.moviting.android.ui.fragment;

import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;
import com.moviting.android.model.Propose;

import java.util.ArrayList;
import java.util.HashMap;

public class ProposeFragment extends BaseFragment {

    private static final String TAG = "ProposeFragment";
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private ArrayList<Propose> mProposeList;
    private TextView tvBlankPage;
    private HashMap<String, String> prevSnapshot;
    private DatabaseReference ref;

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String proposeStatus = ((HashMap<String, String>) dataSnapshot.getValue()).get("status");

            if(!proposeStatus.equals("Dislike") && !proposeStatus.equals("Matched")) {
                Propose propose = new Propose();
                propose.setUid(dataSnapshot.getKey());
                propose.setStatus(proposeStatus);
                ((ProposePageAdapter)mPagerAdapter).addItem(propose);
                mPagerAdapter.notifyDataSetChanged();
                tvBlankPage.setVisibility(View.GONE);
                addSnapshotToPrevMap(dataSnapshot.getKey(), proposeStatus);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String proposeStatus = ((HashMap<String, String>) dataSnapshot.getValue()).get("status");
            if(proposeStatus.equals("Proposed") && (getPrevStatus(dataSnapshot.getKey()) == null || !getPrevStatus(dataSnapshot.getKey()).equals("Like"))) {
                Propose propose = new Propose();
                propose.setUid(dataSnapshot.getKey());
                propose.setStatus(proposeStatus);
                ((ProposePageAdapter)mPagerAdapter).addItem(propose);
                mPagerAdapter.notifyDataSetChanged();
                tvBlankPage.setVisibility(View.GONE);
            }
            addSnapshotToPrevMap(dataSnapshot.getKey(), proposeStatus);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

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
        prevSnapshot = new HashMap<>();
        mPagerAdapter = new ProposePageAdapter(getChildFragmentManager());
        addProposeListner();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_propose, container, false);
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        tvBlankPage = (TextView) rootView.findViewById(R.id.blank_page);

        if(mProposeList.size() != 0) {
            tvBlankPage.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        try {
            super.onDestroy();
        } catch (NullPointerException npe) {
            Log.e(TAG, "NPE: Bug workaround");
        }
        ref.removeEventListener(childEventListener);
    }

    private void addSnapshotToPrevMap(String key, String value) {
        prevSnapshot.put( key, value );
    }

    @Nullable
    private String getPrevStatus(String key) {
        if(prevSnapshot.size() != 0) {
            return prevSnapshot.get(key);
        } else {
            return null;
        }
    }

    private void addProposeListner() {
        ref = getBaseActivity().getFirebaseDatabaseReference().child("propose").child(getBaseActivity().getUid());
        ref.addChildEventListener(childEventListener);
    }

    public void updateProposeStatus(String proposeStatus, int index) {
        Propose propose = mProposeList.get(index);
        propose.setStatus(proposeStatus);
        mPagerAdapter.notifyDataSetChanged();
    }

    public void deleteProposeFromList(int index){
        ((ProposePageAdapter)mPagerAdapter).removeItem(index);
        mPagerAdapter.notifyDataSetChanged();
        if(mProposeList.size() == 0) {
            tvBlankPage.setVisibility(View.VISIBLE);
        }
    }

    private class ProposePageAdapter extends FragmentStatePagerAdapter {
        public ProposePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ProposePageFragment proposePageFragment = ProposePageFragment.newInstance(mProposeList.get(position).getUid(), mProposeList.get(position).getStatus(), position);

            return proposePageFragment;
        }

        @Override
        public int getCount() {
            return mProposeList.size();
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }

        public void addItem(Propose propose) {
            mProposeList.add(propose);
        }

        public void removeItem(int index) {
            if(mProposeList.size() != 0) {
                mProposeList.remove(index);
            }

            if(mProposeList.size() == 0) {
                tvBlankPage.setVisibility(View.VISIBLE);
            }
        }
    }
}

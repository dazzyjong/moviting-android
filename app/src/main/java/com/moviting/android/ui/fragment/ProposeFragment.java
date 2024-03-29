package com.moviting.android.ui.fragment;

import android.os.Parcelable;
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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;
import com.moviting.android.model.Propose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProposeFragment extends BaseFragment {

    private static final String TAG = "ProposeFragment";
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private CopyOnWriteArrayList<Propose> mProposeList;
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
            } else if(proposeStatus.equals("Matched")) {
                deleteProposeFromList(dataSnapshot.getKey());
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
            if(getActivity() != null && isAdded()) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
            Log.w(TAG, databaseError.getDetails());
        }
    };

    public ProposeFragment() {
        // Required empty public constructor
    }

    public static ProposeFragment newInstance() {
        return new ProposeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mProposeList = new CopyOnWriteArrayList<>();
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
        ref = getFirebaseDatabaseReference().child("propose").child(getUid());
        ref.addChildEventListener(childEventListener);
    }

    public void updateProposeStatus(String proposeStatus, int index) {
        if(mProposeList.size() > 0) {
            Propose propose = mProposeList.get(index);
            propose.setStatus(proposeStatus);
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void deleteProposeFromList(int index){
        ((ProposePageAdapter)mPagerAdapter).removeItem(index);
        mPagerAdapter.notifyDataSetChanged();
        if(mProposeList.size() == 0) {
            tvBlankPage.setVisibility(View.VISIBLE);
        }
    }

    public void deleteProposeFromList(String uid){
        for(Propose item :mProposeList ){
            if(item.getUid().equals(uid)) {
                int index = mProposeList.indexOf(item);
                ((ProposePageAdapter)mPagerAdapter).removeItem(index);
                mPagerAdapter.notifyDataSetChanged();
            }
        }

        if(mProposeList.size() == 0) {
            tvBlankPage.setVisibility(View.VISIBLE);
        }
    }

    private class ProposePageAdapter extends FragmentStatePagerAdapter {
        ProposePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ProposePageFragment.newInstance(mProposeList.get(position).getUid(), mProposeList.get(position).getStatus(), position);
        }

        @Override
        public int getCount() {
            return mProposeList.size();
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }

        void addItem(Propose propose) {
            mProposeList.add(propose);
        }

        void removeItem(int index) {
            if(mProposeList.size() != 0) {
                mProposeList.remove(index);
            }

            if(mProposeList.size() == 0) {
                tvBlankPage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}

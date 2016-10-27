package com.moviting.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.moviting.android.ui.activity.ChatActivity;
import com.moviting.android.ui.activity.InfoBeforePaymentActivity;
import com.moviting.android.util.MyHashMap;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchFragment extends BaseFragment {

    private static final String TAG = "MatchFragment";
    private static final int LIKE_EACH_OTHER = 0;
    private static final int MATCH_PROGRESS = 1;
    private static final int MATCH_COMPLETE = 2;

    private static final int REQUEST_PAYMENT = 100;

    private LinearLayoutManager mLikeEachOtherLM;
    private RecyclerView rvLikeEachOther;

    private LinearLayoutManager mMatchProgressLM;
    private RecyclerView rvMatchProgress;

    private LinearLayoutManager mMatchCompleteLM;
    private RecyclerView rvMatchComplete;

    private MyHashMap<String, MatchInfo> matchUids;
    private String gender;

    private ChildEventListener listEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "user_match added: " + dataSnapshot.getKey());
            Boolean b = (Boolean)dataSnapshot.getValue();
            if(b) {
                matchUids.put(dataSnapshot.getKey(), new MatchInfo(dataSnapshot.getKey()));
                addListenerMatchList(dataSnapshot.getKey());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "user_match changed: " + dataSnapshot.getKey());
            Boolean b = (Boolean)dataSnapshot.getValue();
            if(!b) {
                popMatchUid(dataSnapshot.getKey());
            }
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

    private ChildEventListener mListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if( !hasMatchUid(dataSnapshot.getRef().getParent().getKey())) {

                Log.d(TAG, "match_user_payment added: " + dataSnapshot.getRef().getParent().getKey() + " " + dataSnapshot.getKey());
                MatchInfo matchInfo = matchUids.get(dataSnapshot.getRef().getParent().getKey());

                if (dataSnapshot.getKey().equals(getBaseActivity().getUid())) {
                    HashMap object = (HashMap) dataSnapshot.getValue();
                    matchInfo.myGender = gender;
                    matchInfo.myPayment = (Boolean) object.get("payment");
                    matchInfo.myType = (String) object.get("type");
                } else {
                    matchInfo.opponentUid = dataSnapshot.getKey();
                    HashMap object = (HashMap) dataSnapshot.getValue();
                    matchInfo.opponentPayment = (Boolean) object.get("payment");
                    matchInfo.opponentType = (String) object.get("type");
                    getOpponentInfo(matchInfo);
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "match_user_payment changed: " + dataSnapshot.getRef().getParent().getKey() +" " + dataSnapshot.getKey());
            String matchuid = dataSnapshot.getRef().getParent().getKey();
            MatchInfo info = popMatchUid(matchuid);

            if(info != null) {
                if (dataSnapshot.getKey().equals(getBaseActivity().getUid())) {
                    HashMap object = (HashMap)dataSnapshot.getValue();
                    info.myPayment = (Boolean) object.get("payment");
                    info.myType = (String) object.get("type");
                } else {
                    HashMap object = (HashMap)dataSnapshot.getValue();
                    info.opponentPayment = (Boolean) object.get("payment");
                    info.opponentType = (String) object.get("type");
                }

                if (info.opponentPayment && info.myPayment) {
                    ((OpponentImageAdapter) rvMatchComplete.getAdapter()).addItem(info);
                    rvMatchComplete.getAdapter().notifyDataSetChanged();
                } else if (info.myPayment) {
                    ((OpponentImageAdapter) rvMatchProgress.getAdapter()).addItem(info);
                    rvMatchProgress.getAdapter().notifyDataSetChanged();
                } else if (!info.myPayment) {
                    ((OpponentImageAdapter) rvLikeEachOther.getAdapter()).addItem(info);
                    rvLikeEachOther.getAdapter().notifyDataSetChanged();
                }
            }
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

    public MatchFragment() {
        // Required empty public constructor
    }

    public static MatchFragment newInstance() {
        MatchFragment fragment = new MatchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        matchUids = new MyHashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match, container, false);

        rvLikeEachOther = (RecyclerView) view.findViewById(R.id.rv_like_each_other);
        rvMatchProgress = (RecyclerView) view.findViewById(R.id.rv_match_progress);
        rvMatchComplete = (RecyclerView) view.findViewById(R.id.rv_match_complete);

        rvLikeEachOther.setHasFixedSize(true);
        rvMatchProgress.setHasFixedSize(true);
        rvMatchComplete.setHasFixedSize(true);

        mLikeEachOtherLM = new LinearLayoutManager(getActivity());
        mLikeEachOtherLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        mMatchProgressLM = new LinearLayoutManager(getActivity());
        mMatchProgressLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        mMatchCompleteLM = new LinearLayoutManager(getActivity());
        mMatchCompleteLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvLikeEachOther.setLayoutManager(mLikeEachOtherLM);
        rvMatchProgress.setLayoutManager(mMatchProgressLM);
        rvMatchComplete.setLayoutManager(mMatchCompleteLM);

        rvLikeEachOther.setAdapter(new OpponentImageAdapter(LIKE_EACH_OTHER));
        rvMatchProgress.setAdapter(new OpponentImageAdapter(MATCH_PROGRESS));
        rvMatchComplete.setAdapter(new OpponentImageAdapter(MATCH_COMPLETE));

        getBaseActivity().getFirebaseDatabaseReference().child("users").child(getBaseActivity().getUid()).child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gender = ((String)dataSnapshot.getValue());
                addMatchListListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });

        return view;
    }

    private boolean hasMatchUid(String uid) {
        if ( ((OpponentImageAdapter)rvMatchComplete.getAdapter()).findItem(uid) != -1
                || ((OpponentImageAdapter)rvMatchProgress.getAdapter()).findItem(uid) != -1
                || ((OpponentImageAdapter)rvLikeEachOther.getAdapter()).findItem(uid) != -1) {
            return true;
        }

        return false;
    }

    private MatchInfo popMatchUid(String uid) {
        MatchInfo info = null;
        int index = ((OpponentImageAdapter)rvMatchComplete.getAdapter()).findItem(uid);
        if(index != -1) {
            info = ((OpponentImageAdapter)rvMatchComplete.getAdapter()).getItem(index);
            ((OpponentImageAdapter)rvMatchComplete.getAdapter()).removeItem(index);
            rvMatchComplete.getAdapter().notifyDataSetChanged();

            return info;
        }

        index = ((OpponentImageAdapter)rvMatchProgress.getAdapter()).findItem(uid);
        if(index != -1) {
            info = ((OpponentImageAdapter)rvMatchProgress.getAdapter()).getItem(index);
            ((OpponentImageAdapter)rvMatchProgress.getAdapter()).removeItem(index);
            rvMatchProgress.getAdapter().notifyDataSetChanged();
            return info;
        }

        index = ((OpponentImageAdapter)rvLikeEachOther.getAdapter()).findItem(uid);
        if(index != -1) {
            info = ((OpponentImageAdapter)rvLikeEachOther.getAdapter()).getItem(index);
            ((OpponentImageAdapter)rvLikeEachOther.getAdapter()).removeItem(index);
            rvLikeEachOther.getAdapter().notifyDataSetChanged();
            return info;
        }

        return info;
    }

    private void addListenerMatchList(String matchUid) {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("match_member_payment").child(matchUid).addChildEventListener(mListener);
    }

    private void addMatchListListener() {
            getBaseActivity().getFirebaseDatabaseReference()
                    .child("user_match").child(getBaseActivity().getUid()).addChildEventListener(listEventListener);
    }

    private void getOpponentInfo(final MatchInfo matchInfo) {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(matchInfo.opponentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> object = (HashMap)dataSnapshot.getValue();
                matchInfo.opponentName = (String)object.get("name");
                matchInfo.opponentPhotoPath = (String)object.get("photoUrl");
                matchInfo.opponentGender = (String)object.get("gender");

                if(matchInfo.opponentPayment && matchInfo.myPayment) {
                    ((OpponentImageAdapter)rvMatchComplete.getAdapter()).addItem(matchInfo);
                    rvMatchComplete.getAdapter().notifyDataSetChanged();
                } else if(matchInfo.myPayment) {
                    ((OpponentImageAdapter)rvMatchProgress.getAdapter()).addItem(matchInfo);
                    rvMatchProgress.getAdapter().notifyDataSetChanged();
                } else if(!matchInfo.myPayment) {
                    ((OpponentImageAdapter)rvLikeEachOther.getAdapter()).addItem(matchInfo);
                    rvLikeEachOther.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  REQUEST_PAYMENT) {

        }
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            for(String matchUid:matchUids.keySet()) {
                getBaseActivity().getFirebaseDatabaseReference()
                        .child("match_member_payment").child(matchUid).removeEventListener(mListener);
            }
            getBaseActivity().getFirebaseDatabaseReference()
                    .child("user_match").child(getBaseActivity().getUid()).removeEventListener(listEventListener);
        } catch (NullPointerException npe) {
            Log.e(TAG, "NPE: Bug workaround");
        }
    }

    private class OpponentImageAdapter extends RecyclerView.Adapter<OpponentImageAdapter.ViewHolder> {
        private ArrayList<MatchInfo> mList;
        private int kind;

        OpponentImageAdapter(int kind) {
            mList = new ArrayList<>();
            this.kind = kind;
        }

        @Override
        public OpponentImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_opponent_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Glide.with(getActivity()).load(mList.get(position).opponentPhotoPath).into(holder.mImageView);
            holder.mTextView.setText(mList.get(position).opponentName);

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            TextView  mTextView;

            ViewHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.my_img);
                mTextView = (TextView) itemView.findViewById(R.id.opponent_name);

                if(kind == MATCH_COMPLETE) {
                    mImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(ChatActivity.createIntent(getActivity(), mList.get(getAdapterPosition())));
                        }
                    });
                } else if(kind == MATCH_PROGRESS) {
                    mImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getActivity(), "상대의 결제를 기다리고 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if(kind == LIKE_EACH_OTHER) {
                    mImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivityForResult(InfoBeforePaymentActivity.createIntent(getActivity(), mList.get(getAdapterPosition())), REQUEST_PAYMENT);
                        }
                    });
                }
            }
        }

        void addItem(MatchInfo matchInfo) {
            mList.add(matchInfo);
        }

        void removeItem(int i) {
            mList.remove(i);
        }

        MatchInfo getItem(int i){
            return mList.get(i);
        }

        int findItem(String matchUid) {
            for(int i = 0; i < mList.size(); i++) {
                if(matchUid.equals(mList.get(i).matchUid)) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}

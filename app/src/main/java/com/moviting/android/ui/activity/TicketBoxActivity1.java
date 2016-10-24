package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class TicketBoxActivity1 extends BaseActivity {

    private static final String TAG = "TicketBoxActivity1";
    private LinearLayoutManager mMatchCompleteLM;
    private RecyclerView rvMovieShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_box1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvMovieShare = (RecyclerView) findViewById(R.id.rv_movie_share);

        mMatchCompleteLM = new LinearLayoutManager(getBaseContext());
        mMatchCompleteLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvMovieShare.setLayoutManager(mMatchCompleteLM);
        rvMovieShare.setAdapter(new OpponentImageAdapter());

        getMatchList();
    }

    private void getMatchList() {
        getFirebaseDatabaseReference()
                .child("user_match").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> matchList = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    matchList.add(child.getKey());
                }
                getMatchMember(matchList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getMatchMember(final ArrayList<String> matchIdList){
        for(final String matchUid: matchIdList) {
            getFirebaseDatabaseReference()
                    .child("match_member_payment").child(matchUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    MatchInfo matchInfo = new MatchInfo(matchUid);
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equals(getUid())) {
                            HashMap object = (HashMap)child.getValue();
                            matchInfo.myPayment = (Boolean)object.get("payment");
                            matchInfo.myType = (String) object.get("type");
                        } else {
                            matchInfo.opponentUid = child.getKey();
                            HashMap object = (HashMap)child.getValue();
                            matchInfo.opponentPayment = (Boolean)object.get("payment");
                            matchInfo.opponentType = (String) object.get("type");
                        }
                    }
                    getOpponentInfo(matchInfo);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, databaseError.getDetails());
                }
            });
        }
    }

    private void getOpponentInfo(final MatchInfo matchInfo) {
        getFirebaseDatabaseReference()
                .child("users").child(matchInfo.opponentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //matchInfo.opponentName = dataSnapshot.getValue()
                HashMap<String, Object> object = (HashMap)dataSnapshot.getValue();
                matchInfo.opponentName = (String)object.get("name");
                matchInfo.opponentPhotoPath = (String)object.get("photoUrl");

                if(matchInfo.opponentPayment && matchInfo.myPayment) {
                    ((OpponentImageAdapter)rvMovieShare.getAdapter()).addItem(matchInfo);
                    rvMovieShare.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, TicketBoxActivity1.class);
        return in;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                TicketBoxActivity1.this.onBackPressed();
                break;
        }
        return true;
    }

    private class OpponentImageAdapter extends RecyclerView.Adapter<OpponentImageAdapter.ViewHolder> {
        private ArrayList<MatchInfo> mList;

        OpponentImageAdapter() {
            mList = new ArrayList<>();
        }

        @Override
        public OpponentImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_opponent_item, parent, false);
            OpponentImageAdapter.ViewHolder vh = new OpponentImageAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(OpponentImageAdapter.ViewHolder holder, int position) {
            Glide.with(getBaseContext()).load(mList.get(position).opponentPhotoPath).into(holder.mImageView);
            holder.mTextView.setText(mList.get(position).opponentName);

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            TextView mTextView;

            ViewHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.my_img);
                mTextView = (TextView) itemView.findViewById(R.id.opponent_name);

                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(TicketBoxActivity2.createIntent(getBaseContext(), mList.get(getAdapterPosition())));
                    }
                });
            }
        }

        void addItem(MatchInfo matchInfo) {
            mList.add(matchInfo);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}

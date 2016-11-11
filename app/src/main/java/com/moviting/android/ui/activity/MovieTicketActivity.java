package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.moviting.android.model.MovieTicket;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieTicketActivity extends BaseActivity {

    private static final int SEND_SUCCESS = 100;
    private static final String TAG = "MovieTicketActivity";

    private MatchInfo matchInfo;
    private ArrayList<MovieTicket> movieTicketList;

    private CheckBox ticket1;
    private CheckBox ticket2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_ticket);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        matchInfo = (MatchInfo)getIntent().getSerializableExtra("matchInfo");
        ticket1 = (CheckBox) findViewById(R.id.checkBox1);
        ticket2 = (CheckBox) findViewById(R.id.checkBox2);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ticket1.isChecked()) {
                    if(matchInfo.opponentType.equals("coupon")) {
                        Toast.makeText(getBaseContext(), "쿠폰 사용자에겐 영화표를 전달할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        moveTicket(getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()), movieTicketList.get(0).ticketId,
                                getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(matchInfo.opponentUid));
                    }
                } else if(ticket2.isChecked()) {
                    if(matchInfo.opponentType.equals("coupon")) {
                        Toast.makeText(getBaseContext(), "쿠폰 사용자에겐 영화표를 전달할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        moveTicket(getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()), movieTicketList.get(1).ticketId,
                                getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(matchInfo.opponentUid));
                    }
                } else {
                    Toast.makeText(getBaseContext(), "티켓을 하나 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        movieTicketList = new ArrayList<>();
        getMyTicket();
    }

    public void moveTicket(final DatabaseReference fromPath, final String ticketId, final DatabaseReference toPath)
    {
        fromPath.child(ticketId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                toPath.child(ticketId).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener()
                {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase)
                    {
                        if (firebaseError != null)
                        {
                            Log.d(TAG, "moveTicket() failed. firebaseError = " + firebaseError);
                        }
                        else
                        {
                            fromPath.child(ticketId).removeValue();
                            setResult(SEND_SUCCESS);
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {
                Log.d(TAG, "moveTicket() failed. firebaseError" + firebaseError);
            }
        });
    }

    private void getMyTicket() {
        getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    HashMap object = (HashMap)child.getValue();
                    movieTicketList.add(new MovieTicket(child.getKey(), null, (Boolean)object.get("screening")));
                }

                for(int i = 0; i < movieTicketList.size(); i++) {
                    if(i == 0) {
                        ticket1.setVisibility(View.VISIBLE);
                    }
                    if(i == 1) {
                        ticket2.setVisibility(View.VISIBLE);
                    }
                }
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

    public static Intent createIntent(Context context, MatchInfo matchInfo) {
        Intent in = new Intent();
        in.putExtra("matchInfo", matchInfo);
        in.setClass(context, MovieTicketActivity.class);
        return in;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                MovieTicketActivity.this.onBackPressed();
                break;
        }
        return true;
    }
}

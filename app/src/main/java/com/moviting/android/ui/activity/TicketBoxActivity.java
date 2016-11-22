package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.moviting.android.model.MovieTicket;

import java.util.ArrayList;
import java.util.HashMap;

public class TicketBoxActivity extends BaseActivity {

    private static final String TAG = "TicketBoxActivity";
    private static final int MOVIE_TICKET = 1;
    private MatchInfo matchInfo;
    private ArrayList<MovieTicket> movieTicketList;

    private LinearLayout ticket1Layout;
    private LinearLayout ticket2Layout;

    private Button ticket1Button;
    private Button ticket2Button;

    private TextView noTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_box);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        matchInfo = (MatchInfo)getIntent().getSerializableExtra("matchInfo");
        movieTicketList = new ArrayList<>();

        ticket1Layout = (LinearLayout)findViewById(R.id.ticket1_layout);
        ticket2Layout = (LinearLayout)findViewById(R.id.ticket2_layout);

        ticket1Button = (Button) findViewById(R.id.ticket1_button);
        ticket1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(TicketDetailActivity.createIntent(TicketBoxActivity.this, matchInfo, movieTicketList.get(0)), MOVIE_TICKET);
            }
        });

        ticket2Button = (Button) findViewById(R.id.ticket2_button);
        ticket2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(TicketDetailActivity.createIntent(TicketBoxActivity.this, matchInfo, movieTicketList.get(1)), MOVIE_TICKET);
            }
        });

        noTicket = (TextView) findViewById(R.id.no_ticket);

        getMyTicket();
    }

    private void getMyTicket() {
        getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    HashMap object = (HashMap)child.getValue();
                    movieTicketList.add(new MovieTicket(child.getKey(), (String)object.get("expiration_date"), (Boolean)object.get("screening")));
                }

                if(movieTicketList.size() == 0) {
                    noTicket.setVisibility(View.VISIBLE);
                    ticket1Layout.setVisibility(View.GONE);
                    ticket2Layout.setVisibility(View.GONE);
                }

                for(int i = 0; i < movieTicketList.size(); i++) {
                    if(i == 0) {
                        ticket1Layout.setVisibility(View.VISIBLE);
                    }
                    if(i == 1) {
                        ticket2Layout.setVisibility(View.VISIBLE);
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
        in.setClass(context, TicketBoxActivity.class);
        return in;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                TicketBoxActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MOVIE_TICKET && resultCode == 0) {
            movieTicketList = null;
            movieTicketList = new ArrayList<>();
            getMyTicket();
        }
    }
}

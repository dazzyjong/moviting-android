package com.moviting.android.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.moviting.android.model.MovieTicket;

import java.util.ArrayList;
import java.util.HashMap;

public class TicketBoxActivity2 extends BaseActivity {

    private static final String TAG = "TicketBoxActivity2";
    private MatchInfo matchInfo;
    private ArrayList<MovieTicket> movieTicketList;

    private LinearLayout ticket1Layout;
    private LinearLayout ticket2Layout;

    private Button ticket1Button;
    private Button ticket2Button;
    private Button howToUse;
    private TextView ticket1;
    private TextView ticket2;

    private TextView ticket1ExpirationDate;
    private TextView ticket2ExpirationDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_box2);
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
                if(matchInfo.myType.equals("coupon")){
                    createWarningDialog();
                } else {
                    createDialog(movieTicketList.get(0), 0);
                }
            }
        });

        ticket2Button = (Button) findViewById(R.id.ticket2_button);
        ticket2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(matchInfo.myType.equals("coupon")){
                    createWarningDialog();
                } else {
                    createDialog(movieTicketList.get(1), 1);
                }
            }
        });

        howToUse = (Button) findViewById(R.id.how_to_use);
        howToUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(WebViewActivity.createIntent(TicketBoxActivity2.this, "http://theysy.com/flow_cgv.html"));
            }
        });

        ticket1ExpirationDate = (TextView) findViewById(R.id.expiration_date1);
        ticket2ExpirationDate = (TextView) findViewById(R.id.expiration_date2);

        ticket1 = (TextView) findViewById(R.id.ticket_id1);
        ticket2 = (TextView) findViewById(R.id.ticket_id2);
        getMyTicket();
    }

    private void createWarningDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketBoxActivity2.this);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("쿠폰 결제시 영화표를 열 수 없습니다. 상대에게 영화표를 전달해서 예매를 위임해주세요.")
                .setCancelable(true)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    private void createDialog(final MovieTicket movieTicket, final int index) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketBoxActivity2.this);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage(R.string.movie_ticket_peel)
                .setCancelable(false)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()).child(movieTicket.ticketId).child("screening").setValue(false, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(index == 0) {
                                            ticket1Button.setVisibility(View.GONE);
                                            ticket1.setText(movieTicket.ticketId);
                                        } else if(index == 1){
                                            ticket2Button.setVisibility(View.GONE);
                                            ticket2.setText(movieTicket.ticketId);
                                        }
                                    }
                                });
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    private void getMyTicket() {
        getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    HashMap object = (HashMap)child.getValue();
                    movieTicketList.add(new MovieTicket(child.getKey(), (String)object.get("expiration_date"), (Boolean)object.get("screening")));
                }

                for(int i = 0; i < movieTicketList.size(); i++) {
                    if(i == 0) {
                        ticket1Layout.setVisibility(View.VISIBLE);
                        ticket1ExpirationDate.setText(movieTicketList.get(i).expirationDate);
                        if(!movieTicketList.get(i).screen) {
                            ticket1Button.setVisibility(View.GONE);
                            ticket1.setText(movieTicketList.get(i).ticketId);
                        } else {
                            ticket1.setText("**********");
                        }
                    }
                    if(i == 1) {
                        ticket2Layout.setVisibility(View.VISIBLE);
                        ticket2ExpirationDate.setText(movieTicketList.get(i).expirationDate);
                        if(!movieTicketList.get(i).screen) {
                            ticket2Button.setVisibility(View.GONE);
                            ticket2.setText(movieTicketList.get(i).ticketId);
                        } else {
                            ticket2.setText("**********");
                        }
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
        in.setClass(context, TicketBoxActivity2.class);
        return in;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                TicketBoxActivity2.this.onBackPressed();
                break;
        }
        return true;
    }

}

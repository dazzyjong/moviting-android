package com.moviting.android.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.moviting.android.model.Message;
import com.moviting.android.model.MovieTicket;

public class TicketDetailActivity extends BaseActivity {
    private static final String TAG = "TicketDetailActivity";
    private MatchInfo matchInfo;
    private ListView movieTicketInfoList;
    private MovieTicket movieTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        matchInfo = (MatchInfo) getIntent().getSerializableExtra("matchInfo");
        movieTicket = (MovieTicket)getIntent().getSerializableExtra("movieTicket");
        movieTicketInfoList = (ListView) findViewById(R.id.content_ticket_detail);
        movieTicketInfoList.setAdapter(new MovieTicketInfoAdapter());

        movieTicketInfoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 4) {
                    startActivity(WebViewActivity.createIntent(TicketDetailActivity.this, "http://theysy.com/flow_cgv.html"));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                TicketDetailActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    public static Intent createIntent(Context context, MatchInfo matchInfo, MovieTicket movieTicket) {
        Intent in = new Intent();
        in.putExtra("matchInfo", matchInfo);
        in.putExtra("movieTicket", movieTicket);
        in.setClass(context, TicketDetailActivity.class);
        return in;
    }

    public class MovieTicketInfoAdapter extends BaseAdapter {
        String[] infoList;

        MovieTicketInfoAdapter() {
            infoList = getResources().getStringArray(R.array.movie_info_list);
        }

        @Override
        public int getCount() {
            return infoList.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            MovieTicketInfoViewHolder movieTicketInfoViewHolder;
            if(view == null) {
                movieTicketInfoViewHolder = new MovieTicketInfoViewHolder();
                LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.movie_info_item, null);

                movieTicketInfoViewHolder.key = (TextView) view.findViewById(R.id.tag);
                movieTicketInfoViewHolder.value = (TextView) view.findViewById(R.id.value);
                movieTicketInfoViewHolder.arrow = (ImageView) view.findViewById(R.id.arrow);
                movieTicketInfoViewHolder.button = (Button) view.findViewById(R.id.confirm_button);
                view.setTag(movieTicketInfoViewHolder);
            } else {
                movieTicketInfoViewHolder = (MovieTicketInfoViewHolder) view.getTag();
            }
            movieTicketInfoViewHolder.key.setText(infoList[i]);

            switch (i) {
                case 0:
                    if(!movieTicket.screen) {
                        movieTicketInfoViewHolder.value.setText(movieTicket.ticketId);
                        movieTicketInfoViewHolder.button.setEnabled(false);
                    } else {
                        movieTicketInfoViewHolder.value.setText("************");
                    }
                    movieTicketInfoViewHolder.value.setTextIsSelectable(true);
                    movieTicketInfoViewHolder.button.setVisibility(View.VISIBLE);
                    movieTicketInfoViewHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(matchInfo.myType.equals("coupon")){
                                createWarningDialog();
                            } else {
                                createDialog(movieTicket);
                            }
                        }
                    });
                    break;
                case 5:
                    movieTicketInfoViewHolder.button.setVisibility(View.VISIBLE);
                    movieTicketInfoViewHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                createHandOverDialog();
                        }
                    });
                    break;
                case 1:
                    view.setEnabled(false);
                    view.setOnClickListener(null);
                    movieTicketInfoViewHolder.value.setText(movieTicket.expirationDate);
                    break;
                case 2:
                    view.setEnabled(false);
                    view.setOnClickListener(null);
                    movieTicketInfoViewHolder.value.setText("전국 CGV(청담, 여의도 제외)");
                    break;
                case 3:
                    view.setEnabled(false);
                    view.setOnClickListener(null);
                    movieTicketInfoViewHolder.value.setText("3D 및 특별관 제외");
                    break;
                case 4:
                    movieTicketInfoViewHolder.arrow.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

            return view;
        }

        class MovieTicketInfoViewHolder {
            TextView key;
            TextView value;
            ImageView arrow;
            Button button;
        }
    }

    private void createWarningDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketDetailActivity.this);

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

    private void createHandOverDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketDetailActivity.this);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage(R.string.ask_ticket_send)
                .setTitle("건네주기")
                .setCancelable(true)
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        })
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if(matchInfo.opponentType.equals("coupon")) {
                                    Toast.makeText(getBaseContext(), "쿠폰 사용자에겐 영화표를 전달할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    moveTicket(getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(getUid()), movieTicket.ticketId,
                                            getFirebaseDatabaseReference().child("match_ticket").child(matchInfo.matchUid).child(matchInfo.opponentUid));
                                }
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    private void createDialog(final MovieTicket movieTicket) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketDetailActivity.this);

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
                                        movieTicket.screen = false;
                                        ((MovieTicketInfoAdapter)movieTicketInfoList.getAdapter()).notifyDataSetChanged();
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
                            getFirebaseDatabaseReference().child("match_chat").child(matchInfo.matchUid).push().setValue(new Message(getUid(), "영화표를 전달했습니다. 영화티켓함을 확인해주세요."));
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
}

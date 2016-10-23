package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.moviting.android.model.Message;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";
    private static final int REQUEST_SEND_TICKET = 1;
    private static final int SEND_SUCCESS = 100;
    private static final int MY_MESSAGE = 1;
    private static final int OPPONENT_MESSAGE = 2;
    private RequestManager mGlideRequestManager;

    public static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public MyMessageViewHolder(View v) {
            super(v);
            mView = v;
        }

        void setMyView(String message) {
            TextView myMessage = (TextView) mView.findViewById(R.id.my_message);
            myMessage.setText(message);
        }
    }

    public static class OpponentMessageViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public OpponentMessageViewHolder(View v) {
            super(v);
            mView = v;
        }

        void setOpponentView(String name, String message, String photoUrl, RequestManager requestManager) {
            TextView opponentMessage = (TextView) mView.findViewById(R.id.opponent_message);
            opponentMessage.setText(message);

            TextView opponentNameText = (TextView) mView.findViewById(R.id.opponent_name_text);
            opponentNameText.setText(name);

            CircleImageView opponentImage = (CircleImageView) mView.findViewById(R.id.opponent_img);
            requestManager.load(photoUrl).into(opponentImage);
        }
    }

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> mFirebaseAdapter;
    private EditText mMessageEditText;

    private String myName = null;
    private String opponentName = null;
    private String myImageUrl = null;
    private String opponentImageUrl = null;

    private MatchInfo matchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        matchInfo = (MatchInfo)getIntent().getSerializableExtra("matchInfo");

        mGlideRequestManager = Glide.with(ChatActivity.this);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message friendlyMessage = new Message(getUid(), mMessageEditText.getText().toString());
                getFirebaseDatabaseReference().child("match_chat").child(matchInfo.matchUid).push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        getUserNameAndPhoto();
    }

    private void getUserNameAndPhoto() {
        getFirebaseDatabaseReference().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> object = (HashMap)dataSnapshot.getValue();
                if(object.containsKey("name")) {
                    myName = (String)object.get("name");
                } else {
                    myName = "";
                }

                if(object.containsKey("photoUrl")) {
                    myImageUrl = (String)object.get("photoUrl");
                } else {
                    myImageUrl = "";
                }

                getOpponentNameAndPhoto();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getOpponentNameAndPhoto() {
        getFirebaseDatabaseReference().child("users").child(matchInfo.opponentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> object = (HashMap)dataSnapshot.getValue();
                if(object.containsKey("name")) {
                    opponentName = (String)object.get("name");
                } else {
                    opponentName = "";
                }

                if(object.containsKey("photoUrl")) {
                    opponentImageUrl = (String)object.get("photoUrl");
                } else {
                    opponentImageUrl = "";
                }

                getChatList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getChatList() {
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>(
                Message.class,
                R.layout.opponent_message_item,
                RecyclerView.ViewHolder.class,
                getFirebaseDatabaseReference().child("match_chat").child(matchInfo.matchUid)) {

            @Override
            protected void populateViewHolder(RecyclerView.ViewHolder viewHolder, Message message, int position) {
                if(message.getUid().equals(getUid())){
                    ((MyMessageViewHolder)viewHolder).setMyView(message.getMessage());
                } else {
                    ((OpponentMessageViewHolder)viewHolder).setOpponentView(opponentName, message.getMessage(), opponentImageUrl, mGlideRequestManager);
                }
            }

            @Override
            public int getItemViewType(int position) {
                Message message = getItem(position);
                if(message.getUid().equals(getUid())){
                    // Layout for an item with an image
                    return MY_MESSAGE;
                } else if(!message.getUid().equals(getUid())){
                    // Layout for an item without an image
                    return OPPONENT_MESSAGE;
                }
                return super.getItemViewType(position);
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case MY_MESSAGE:
                        View userType1 = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.my_message_item, parent, false);
                        return new MyMessageViewHolder(userType1);
                    case OPPONENT_MESSAGE:
                        View userType2 = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.opponent_message_item, parent, false);
                        return new OpponentMessageViewHolder(userType2);
                }
                return super.onCreateViewHolder(parent, viewType);
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                Log.d(TAG, friendlyMessageCount + " " + lastVisiblePosition + " " + positionStart);

                mLinearLayoutManager.smoothScrollToPosition(mMessageRecyclerView, null, mFirebaseAdapter.getItemCount());
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    public static Intent createIntent(Context context, MatchInfo matchInfo) {
        Intent in = new Intent();
        in.putExtra("matchInfo", matchInfo);
        in.setClass(context, ChatActivity.class);
        return in;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ChatActivity.this.onBackPressed();
                break;

            case R.id.send_movie_ticket:
                if(!matchInfo.opponentType.equals("coupon")) {
                    startActivityForResult(MovieTicketActivity.createIntent(getBaseContext(), matchInfo), REQUEST_SEND_TICKET);
                } else {
                    Toast.makeText(getBaseContext(), "쿠폰 사용자에겐 영화표를 전달할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SEND_TICKET) {
            if (resultCode == SEND_SUCCESS) {
                getFirebaseDatabaseReference().child("match_chat").child(matchInfo.matchUid).push().setValue(new Message(getUid(), "영화표를 전달했습니다. 영화티켓함을 확인해주세요."));
            }
        }
    }
}

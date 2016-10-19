package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView opponentMessage;
        public TextView opponentNameText;
        public CircleImageView opponentImage;

        public TextView myMessage;
        public TextView myNameText;
        public CircleImageView myImage;

        public LinearLayout myLayout;
        public LinearLayout opponentLayout;

        public MessageViewHolder(View v) {
            super(v);
            opponentLayout = (LinearLayout) itemView.findViewById(R.id.opponent_message_layout);
            myLayout = (LinearLayout) itemView.findViewById(R.id.my_message_layout);

            opponentMessage = (TextView) itemView.findViewById(R.id.opponent_message);
            opponentNameText = (TextView) itemView.findViewById(R.id.opponent_name_text);
            opponentImage = (CircleImageView) itemView.findViewById(R.id.opponent_img);

            myMessage = (TextView) itemView.findViewById(R.id.my_message);
            myNameText = (TextView) itemView.findViewById(R.id.my_name_text);
            myImage = (CircleImageView) itemView.findViewById(R.id.my_img);
        }
    }

    private Button mSendButton;
    private Button mTicketSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private EditText mMessageEditText;

    private String myName = null;
    private String opponentName = null;
    private String myImageUrl = null;
    private String opponentImageUrl = null;

    private MatchInfo matchInfo;
    private Animation animation;
    private boolean fabClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        matchInfo = (MatchInfo)getIntent().getSerializableExtra("matchInfo");

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

        mTicketSendButton = (Button) findViewById(R.id.send_ticket);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        mTicketSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!matchInfo.opponentType.equals("coupon")) {
                    startActivityForResult(MovieTicketActivity.createIntent(getBaseContext(), matchInfo), REQUEST_SEND_TICKET);
                } else {
                    Toast.makeText(getBaseContext(), "쿠폰 사용자에겐 영화표를 전달할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getUserNameAndPhoto();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!fabClicked) {
                    mTicketSendButton.setVisibility(View.VISIBLE);
                    mTicketSendButton.startAnimation(animation);
                    fabClicked = true;
                } else {
                    mTicketSendButton.setVisibility(View.INVISIBLE);
                    fabClicked = false;
                }
            }
        });
        fabClicked = false;
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

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.message_item,
                MessageViewHolder.class,
                getFirebaseDatabaseReference().child("match_chat").child(matchInfo.matchUid)) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message message, int position) {
                if(message.getUid().equals(getUid())){
                    viewHolder.opponentLayout.setVisibility(View.GONE);
                    viewHolder.myMessage.setText(message.getMessage());
                    viewHolder.myNameText.setText(myName);
                } else {
                    viewHolder.myLayout.setVisibility(View.GONE);
                    viewHolder.opponentMessage.setText(message.getMessage());
                    viewHolder.opponentNameText.setText(opponentName);
                }

                if (myImageUrl == null ) {
                viewHolder.myImage.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                        R.drawable.profile_placeholder));
                } else {
                    Glide.with(ChatActivity.this)
                            .load(myImageUrl)
                            .into(viewHolder.myImage);
                }

                if (opponentImageUrl == null ) {
                    viewHolder.opponentImage.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                            R.drawable.profile_placeholder));
                } else {
                    Glide.with(ChatActivity.this)
                            .load(opponentImageUrl)
                            .into(viewHolder.opponentImage);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
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
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mTicketSendButton.setVisibility(View.INVISIBLE);
        fabClicked = false;
        if(requestCode == REQUEST_SEND_TICKET) {
            if (resultCode == SEND_SUCCESS) {
                getFirebaseDatabaseReference().child("match_chat").child(matchInfo.matchUid).push().setValue(new Message(getUid(), "영화표를 전달했습니다. 영화티켓함을 확인해주세요."));
            }
        }
    }
}

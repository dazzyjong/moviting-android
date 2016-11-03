package com.moviting.android.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.User;
import com.moviting.android.model.UserPreference;
import com.moviting.android.util.ArraySetOperator;
import com.moviting.android.util.MyHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.gujun.android.taggroup.TagGroup;

public class OpponentProfileActivity extends BaseActivity {

    private static final String TAG = "OppoProfileActivity";

    private RecyclerView rvProfileMenu;
    private ImageView imageView;
    private User user;
    private String opponentUid;
    private ArrayList<UserPreference> couplePreference;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponent_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        opponentUid = getIntent().getStringExtra("opponentUid");

        couplePreference = new ArrayList<>();

        rvProfileMenu = (RecyclerView) findViewById(R.id.opponent_profile_property_list);
        rvProfileMenu.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rvProfileMenu.setLayoutManager(mLayoutManager);

        getFirebaseDatabaseReference().child("users").child(opponentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                UserPreference userPreference = dataSnapshot.getValue(UserPreference.class);
                setIntersection(userPreference);
                Map map = user.toMap();

                imageView = (ImageView) findViewById(R.id.imageView);
                Glide.with(getBaseContext()).load(user.photoUrl).into(imageView);
                getSupportActionBar().setTitle(user.name);
                rvProfileMenu.setAdapter(new ProfileAdapter(map));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });

        getFirebaseDatabaseReference()
                .child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserPreference userPreference = dataSnapshot.getValue(UserPreference.class);
                setIntersection(userPreference);
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

    private void setIntersection(UserPreference userPreference) {
        couplePreference.add(userPreference);

        if(couplePreference.size() == 2 ) {
            ((ProfileAdapter) rvProfileMenu.getAdapter()).addIntersectionArray(
                    ArraySetOperator.intersection(couplePreference.get(0).preferredMovie, couplePreference.get(1).preferredMovie),
                    ArraySetOperator.intersection(couplePreference.get(0).preferredDate, couplePreference.get(1).preferredDate));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                OpponentProfileActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Intent createIntent(Context context, String opponentUid) {
        Intent in = new Intent();
        in.putExtra("opponentUid", opponentUid);
        in.setClass(context, OpponentProfileActivity.class);
        return in;
    }

    public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_INTERSECTION = 2;

        String[] profileList;
        private MyHashMap<String, Object> userProfile;
        private UserPreference intersection;

        ProfileAdapter(Map map) {
            userProfile = new MyHashMap<>(map);
            profileList = getResources().getStringArray(R.array.opponent_profile_list);
            intersection = new UserPreference();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_ITEM) {
                ProfileAdapter.ProfileViewHolder profileViewHolder;

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.opponent_profile_item, parent, false);

                profileViewHolder = new ProfileAdapter.ProfileViewHolder(view);
                view.setTag(profileViewHolder);

                return profileViewHolder;
            } else if(viewType ==TYPE_INTERSECTION){
                IntersectionViewHolder intersectionViewHolder;

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.intersection_profile_item, parent, false);

                intersectionViewHolder = new IntersectionViewHolder(view);
                view.setTag(intersectionViewHolder);

                return intersectionViewHolder;
            }else {
                ProfileAdapter.SeparatorViewHolder separatorViewHolder;

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.separator_item, parent, false);

                separatorViewHolder = new SeparatorViewHolder(view);

                return separatorViewHolder;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if(position == 0 || position == 3 || position == 10) {
                SeparatorViewHolder separatorViewHolder = (SeparatorViewHolder)holder;
                separatorViewHolder.separatorText.setText(profileList[position]);
            } else if (position == 1 || position == 2) {
                IntersectionViewHolder intersectionViewHolder = (IntersectionViewHolder) holder;
                intersectionViewHolder.key.setText(profileList[position]);
                ArrayList<String> item = getIntersectionValue(profileList[position]);
                if(item.size() > 3) {
                    List<String> input = item.subList(0,4);
                    intersectionViewHolder.mTagGroup.setTags(input);
                } else if(item.size() > 0 ) {
                    intersectionViewHolder.mTagGroup.setTags(item);
                }
            } else {
                ProfileViewHolder profileViewHolder = (ProfileViewHolder)holder;
                profileViewHolder.key.setText(profileList[position]);
                profileViewHolder.value.setText(getValue(profileList[position]));
            }

            if (position == 1 || position == 2) {

                holder.itemView.setClickable(true);
                holder.itemView.setFocusable(true);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createDialog(position);
                    }
                });
            }
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return profileList.length;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0 || position == 3 || position == 10) {
                return TYPE_SEPARATOR;
            } else if(position == 1 || position == 2) {
                return TYPE_INTERSECTION;
            }

            return TYPE_ITEM;
        }

        class ProfileViewHolder extends RecyclerView.ViewHolder {
            TextView key;
            TextView value;

            public ProfileViewHolder(View itemView) {
                super(itemView);

                key = (TextView) itemView.findViewById(R.id.tag);
                value = (TextView) itemView.findViewById(R.id.value);
            }
        }

        class SeparatorViewHolder extends RecyclerView.ViewHolder {
            TextView separatorText;
            public SeparatorViewHolder(View itemView) {
                super(itemView);
                separatorText = (TextView) itemView.findViewById(R.id.textSeparator);
            }
        }

        class IntersectionViewHolder extends RecyclerView.ViewHolder {
            TextView key;
            TagGroup mTagGroup;
            public IntersectionViewHolder(View itemView) {
                super(itemView);
                key = (TextView) itemView.findViewById(R.id.tag);
                mTagGroup = (TagGroup) itemView.findViewById(R.id.tag_group);
            }
        }

        private void addIntersectionArray(ArrayList<String> movie, ArrayList<String> date) {
            intersection.preferredDate = date;
            intersection.preferredMovie = movie;
            notifyDataSetChanged();
        }

        private String getValue(String key) {
            if(key.equals("이름")) {
                return userProfile.get("name").toString();
            }
            if(key.equals("나이")) {
                return userProfile.get("myAge").toString();
            }
            if(key.equals("키")) {
                return userProfile.get("height").toString();
            }
            if(key.equals("학교")) {
                return userProfile.get("school").toString();
            }
            if(key.equals("직업")) {
                return userProfile.get("work").toString();
            }
            if(key.equals("인생 영화")) {
                return userProfile.get("favoriteMovie").toString();
            }
            return null;
        }

        public ArrayList<String> getIntersectionValue(String key) {
            if(key.equals("영화")) {
                return intersection.preferredMovie;
            }
            if(key.equals("일자")) {
                return intersection.preferredDate;
            }
            return null;
        }

        public UserPreference getIntersection() {
            return intersection;
        }
    }

    private void createDialog(int position) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OpponentProfileActivity.this);

        ArrayAdapter<String> adapter;
        if(position == 1) {
            adapter = new ArrayAdapter<>(this, R.layout.simple_list_item,
                    ((ProfileAdapter) rvProfileMenu.getAdapter()).getIntersection().preferredMovie.toArray(new String[0]) );

        } else {
            adapter = new ArrayAdapter<>(this, R.layout.simple_list_item, R.id.text1,
                    ((ProfileAdapter) rvProfileMenu.getAdapter()).getIntersection().preferredDate.toArray(new String[0]));
        }

        // AlertDialog 셋팅
        alertDialogBuilder
                .setAdapter(adapter, null)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }
}

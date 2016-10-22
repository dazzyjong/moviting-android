package com.moviting.android.ui.activity;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.User;
import com.moviting.android.util.MyHashMap;

import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private static final int REQUEST_EDIT = 1;
    private static final String TAG = "ProfileActivity";
    private ListView profileList;
    private MyHashMap<String, Object> userProfile;
    private ImageView imageView;
    private User user;
    private String opponentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        opponentUid = getIntent().getStringExtra("opponentUid");

        if(opponentUid == null) {
            getFirebaseDatabaseReference().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    Map map = user.toMap();
                    userProfile = new MyHashMap<>(map);

                    profileList = (ListView) findViewById(R.id.profile_property_list);
                    profileList.setAdapter(new ProfileAdapter());

                    profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String selected = getResources().getStringArray(R.array.profile_list)[i];
                            startActivityForResult(
                                    ProfilePropEditActivity.createIntent(getBaseContext(), selected, getValue(selected)),
                                    REQUEST_EDIT);
                        }
                    });
                    imageView = (ImageView) findViewById(R.id.imageView);
                    Glide.with(getBaseContext()).load(userProfile.get("photoUrl")).into(imageView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                }
            });
        } else {
            getFirebaseDatabaseReference().child("users").child(opponentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    Map map = user.toMap();
                    userProfile = new MyHashMap<>(map);

                    profileList = (ListView) findViewById(R.id.profile_property_list);
                    profileList.setAdapter(new ProfileAdapter());

                    imageView = (ImageView) findViewById(R.id.imageView);
                    Glide.with(getBaseContext()).load(userProfile.get("photoUrl")).into(imageView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ProfileActivity.this.onBackPressed();
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
        in.setClass(context, ProfileActivity.class);
        return in;
    }

    public class ProfileAdapter extends BaseAdapter {
        String[] profileList;

        ProfileAdapter() {
            profileList = getResources().getStringArray(R.array.profile_list);
        }
        @Override
        public int getCount() {
            return profileList.length;
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            ProfileAdapter.ProfileViewHolder profileViewHolder;
            if(view == null) {
                profileViewHolder = new ProfileAdapter.ProfileViewHolder();

                LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.profile_item, null);

                profileViewHolder.key = (TextView) view.findViewById(R.id.tag);
                profileViewHolder.value = (TextView) view.findViewById(R.id.value);
                profileViewHolder.arrow = (ImageView) view.findViewById(R.id.arrow);

                view.setTag(profileViewHolder);
            } else {
                profileViewHolder = (ProfileAdapter.ProfileViewHolder) view.getTag();
            }

            profileViewHolder.key.setText(profileList[i]);
            profileViewHolder.value.setText(getValue(profileList[i]));
            if(opponentUid != null){
                profileViewHolder.arrow.setVisibility(View.GONE);
            }

            return view;

        }

        class ProfileViewHolder {
            TextView key;
            TextView value;
            ImageView arrow;
        }
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
        return "";
    }

    private String getPropertyName(String key) {
        if(key.equals("이름")) {
            return "name";
        }
        if(key.equals("나이")) {
            return "myAge";
        }
        if(key.equals("키")) {
            return "height";
        }
        if(key.equals("학교")) {
            return "school";
        }
        if(key.equals("직업")) {
            return "work";
        }
        if(key.equals("인생 영화")) {
            return "favoriteMovie";
        }
        return "";
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                String key = data.getStringExtra("key");
                String value = data.getStringExtra("value");
                userProfile.put(getPropertyName(key), (Object)value);
                ((ProfileAdapter)profileList.getAdapter()).notifyDataSetChanged();
            }
        }
    }
}

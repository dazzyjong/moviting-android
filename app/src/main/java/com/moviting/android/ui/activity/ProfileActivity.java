package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviting.android.R;
import com.moviting.android.model.User;

import java.util.ArrayList;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private ArrayList<String> keyArray = new ArrayList<>();
    private ArrayList<String> valArray = new ArrayList<>();
    private RecyclerView rvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvProfile = (RecyclerView)findViewById(R.id.profile_recycler_view);
        rvProfile.setLayoutManager(new LinearLayoutManager(this));
        rvProfile.setAdapter(new ProfileAdapter());

        Map<String, Object> userValue = User.getUserInstance().toMap();
        for(String key: userValue.keySet()) {
            keyArray.add(key);
            if(userValue.get(key) instanceof Integer){
                valArray.add( String.valueOf(userValue.get(key)));
            } else {
                valArray.add((String)userValue.get(key));
            }
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

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, ProfileActivity.class);
        return in;
    }

    public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.profile_item, parent, false);
            return new ProfileAdapter.ProfileViewHolder(view);
        }

        public class ProfileViewHolder extends RecyclerView.ViewHolder {
            TextView key;
            TextView value;

            public ProfileViewHolder(View itemView) {
                super(itemView);

                key = (TextView) itemView.findViewById(R.id.profile_key);
                value = (TextView) itemView.findViewById(R.id.profile_value);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ProfileAdapter.ProfileViewHolder profileViewHolder = (ProfileAdapter.ProfileViewHolder)holder;
            profileViewHolder.key.setText(keyArray.get(position));
            profileViewHolder.value.setText(valArray.get(position));
        }

        @Override
        public int getItemCount() {
            return keyArray.size();
        }
    }
}

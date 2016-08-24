package com.moviting.android.ui.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviting.android.R;
import com.moviting.android.model.User;

import java.util.ArrayList;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileFragment extends Fragment {

    private ArrayList<String> keyArray = new ArrayList<>();
    private ArrayList<String> valArray = new ArrayList<>();

    private RecyclerView rvProfile;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        rvProfile = (RecyclerView)view.findViewById(R.id.profile_recycler_view);
        rvProfile.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvProfile.setAdapter(new ProfileAdapter());

        Map<String, Object> userValue = User.getUserInstance().toMap();
        for(String key: userValue.keySet()) {
            keyArray.add(key);
            valArray.add((String)userValue.get(key));
        }

        return view;
    }


    public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.profile_item, parent, false);
            return new ProfileViewHolder(view);
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
            ProfileViewHolder profileViewHolder = (ProfileViewHolder)holder;
            profileViewHolder.key.setText(keyArray.get(position));
            profileViewHolder.value.setText(valArray.get(position));
        }

        @Override
        public int getItemCount() {
            return keyArray.size();
        }
    }
}

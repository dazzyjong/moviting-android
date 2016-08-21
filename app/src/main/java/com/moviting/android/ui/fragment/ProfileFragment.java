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

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileFragment extends Fragment {

    private String[] key = new String[]{"key1", "key2", "key3"};
    private String[] value = new String[]{"value1", "value2", "value3"};

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
            profileViewHolder.key.setText(key[position]);
            profileViewHolder.value.setText(value[position]);
        }

        @Override
        public int getItemCount() {
            return key.length;
        }
    }
}

package com.moviting.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.moviting.android.R;
import com.moviting.android.ui.activity.MovieApplicationActivity;

public class SelectSessionFragment extends Fragment {

    public SelectSessionFragment() {
    }

    public static SelectSessionFragment newInstance() {
        SelectSessionFragment fragment = new SelectSessionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_session, container, false);

        ImageButton movieSessionButton = (ImageButton) view.findViewById(R.id.select_movie_session);
        movieSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MovieApplicationActivity.createIntent(getActivity()));
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

package com.moviting.android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviting.android.R;

public class ProposePageFragment extends Fragment {

    private static final String TAG = "ProposePageFragment";
    private static final String ARG_PAGE = "position";
    private int mPageNumber;

    public ProposePageFragment() {
        // Required empty public constructor
    }

    public static ProposePageFragment newInstance(int position) {
        ProposePageFragment fragment = new ProposePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_propose_page, container, false);
        Log.d(TAG, String.valueOf(this));
        ((TextView) view.findViewById(R.id.page)).setText(String.valueOf(mPageNumber + 1));
        return view;
    }
}

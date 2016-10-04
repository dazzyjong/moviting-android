package com.moviting.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.Propose;
import com.moviting.android.model.User;

public class ProposePageFragment extends BaseFragment {

    public int page = -1;

    private static final String TAG = "ProposePageFragment";
    private static final String ARG_PROPOSE_UID = "propose_uid";
    private static final String ARG_PROPOSE_STATUS = "propose_status";

    private String mProposeUid;
    private String mProposeStatus;

    private TextView mAgeAndWork;
    private TextView mName;
    private TextView mFavoriteMovie;

    private ImageButton mLikeButton;
    private ImageButton mDislikeButton;

    public ProposePageFragment() {
        // Required empty public constructor
    }

    public static ProposePageFragment newInstance(String proposeUid, String proposeStatus) {
        ProposePageFragment fragment = new ProposePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROPOSE_UID, proposeUid);
        args.putString(ARG_PROPOSE_STATUS, proposeStatus);
        fragment.setArguments(args);
        Log.d(TAG, "newInstance: " + proposeUid);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProposeUid = getArguments().getString(ARG_PROPOSE_UID);
        mProposeStatus = getArguments().getString(ARG_PROPOSE_STATUS);
        Log.d(TAG, "onCreate: " + mProposeUid + " / " + mProposeStatus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_propose_page, container, false);

        mName = (TextView) view.findViewById(R.id.name);
        mAgeAndWork = (TextView) view.findViewById(R.id.age_and_work);
        mFavoriteMovie = ((TextView) view.findViewById(R.id.favorite_movie));

        mDislikeButton = (ImageButton) view.findViewById(R.id.dislike);
        mLikeButton = (ImageButton) view.findViewById(R.id.like);

        if(mProposeStatus.equals(Propose.ProposeStatus.Like.name())) {
            mDislikeButton.setVisibility(View.GONE);
        }

        mDislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseActivity().getFirebaseDatabaseReference().child("propose").child(getBaseActivity().getUid())
                        .child(mProposeUid).child("status").setValue(Propose.ProposeStatus.Dislike.name())
                        .addOnCompleteListener(getBaseActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getBaseActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    ((ProposeFragment) getParentFragment()).deleteProposeFromList(page);
                                }
                            }
                });
            }
        });

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseActivity().getFirebaseDatabaseReference().child("propose").child(getBaseActivity().getUid())
                        .child(mProposeUid).child("status").setValue(Propose.ProposeStatus.Like.name())
                        .addOnCompleteListener(getBaseActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getBaseActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    mProposeStatus = Propose.ProposeStatus.Like.name();
                                    ((ProposeFragment) getParentFragment()).updateProposeStatus(Propose.ProposeStatus.Like.name(), page);
                                    mDislikeButton.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        getOppositeUserInfo();
        return view;
    }

    public void getOppositeUserInfo() {
        getBaseActivity().getFirebaseDatabaseReference().child("users").child(mProposeUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                mName.setText(user.name);
                mAgeAndWork.setText(user.myAge + " / " + user.work);
                mFavoriteMovie.setText(user.favoriteMovie);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getOppositeUserInfo:onCancelled", databaseError.toException());
            }
        });
    }
}

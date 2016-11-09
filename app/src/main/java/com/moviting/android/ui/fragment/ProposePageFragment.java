package com.moviting.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.Propose;
import com.moviting.android.model.User;
import com.moviting.android.ui.activity.OpponentProfileActivity;
import com.moviting.android.ui.activity.ProfileActivity;

public class ProposePageFragment extends BaseFragment {

    private int page = -1;

    private static final String TAG = "ProposePageFragment";
    private static final String ARG_PROPOSE_UID = "propose_uid";
    private static final String ARG_PROPOSE_STATUS = "propose_status";
    private static final String ARG_PROPOSE_PAGE = "propose_page";

    private String mProposeUid;
    private String mProposeStatus;

    private TextView mAgeAndWork;
    private TextView mName;
    private TextView mFavoriteMovie;

    private ImageButton mLikeButton;
    private ImageButton mDislikeButton;

    private ImageView profileImage;

    public ProposePageFragment() {
        // Required empty public constructor
    }

    public static ProposePageFragment newInstance(String proposeUid, String proposeStatus, int page) {
        ProposePageFragment fragment = new ProposePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROPOSE_UID, proposeUid);
        args.putString(ARG_PROPOSE_STATUS, proposeStatus);
        args.putInt(ARG_PROPOSE_PAGE, page);
        fragment.setArguments(args);
        Log.d(TAG, "newInstance: " + proposeUid);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProposeUid = getArguments().getString(ARG_PROPOSE_UID);
        mProposeStatus = getArguments().getString(ARG_PROPOSE_STATUS);
        page = getArguments().getInt(ARG_PROPOSE_PAGE);
        Log.d(TAG, "onCreate: " + mProposeUid + " / " + mProposeStatus + " / " + page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_propose_page, container, false);

        mName = (TextView) view.findViewById(R.id.name);
        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(OpponentProfileActivity.createIntent(getActivity(), mProposeUid));
            }
        });
        mAgeAndWork = (TextView) view.findViewById(R.id.age_and_work);
        mFavoriteMovie = ((TextView) view.findViewById(R.id.favorite_movie));

        mDislikeButton = (ImageButton) view.findViewById(R.id.dislike);
        mLikeButton = (ImageButton) view.findViewById(R.id.like);

        profileImage = (ImageView) view.findViewById(R.id.opponent_profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(OpponentProfileActivity.createIntent(getActivity(), mProposeUid));
            }
        });

        if(mProposeStatus.equals(Propose.ProposeStatus.Like.name())) {
            mDislikeButton.setVisibility(View.GONE);
        }

        mDislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProposeFragment)getParentFragment()).showProgressDialog();
                getFirebaseDatabaseReference().child("propose").child(getUid())
                        .child(mProposeUid).child("status").setValue(Propose.ProposeStatus.Dislike.name())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    ((ProposeFragment) getParentFragment()).deleteProposeFromList(page);
                                }
                                ((ProposeFragment)getParentFragment()).hideProgressDialog();
                            }
                });
            }
        });

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProposeFragment)getParentFragment()).showProgressDialog();

                if(mProposeStatus.equals(Propose.ProposeStatus.Like.name())) {
                    getFirebaseDatabaseReference().child("propose").child(getUid())
                            .child(mProposeUid).child("status").setValue(Propose.ProposeStatus.Proposed.name())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    } else {
                                        mProposeStatus = Propose.ProposeStatus.Proposed.name();
                                        ((ProposeFragment) getParentFragment()).updateProposeStatus(Propose.ProposeStatus.Proposed.name(), page);
                                        mDislikeButton.setVisibility(View.VISIBLE);
                                    }
                                    ((ProposeFragment)getParentFragment()).hideProgressDialog();
                                }
                            });
                } else {
                    getFirebaseDatabaseReference().child("propose").child(getUid())
                            .child(mProposeUid).child("status").setValue(Propose.ProposeStatus.Like.name())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    } else {
                                        mProposeStatus = Propose.ProposeStatus.Like.name();
                                        ((ProposeFragment) getParentFragment()).updateProposeStatus(Propose.ProposeStatus.Like.name(), page);
                                        mDislikeButton.setVisibility(View.GONE);
                                    }
                                    ((ProposeFragment)getParentFragment()).hideProgressDialog();
                                }
                            });
                }
            }
        });

        getOppositeUserInfo();
        return view;
    }

    public void getOppositeUserInfo() {
        getFirebaseDatabaseReference().child("users").child(mProposeUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null) {
                    mName.setText(Html.fromHtml("<u>" + user.name + "</u>"));
                    mAgeAndWork.setText(user.myAge + " / " + user.work);
                    mFavoriteMovie.setText(user.favoriteMovie);
                    Glide.with(getParentFragment().getActivity()).load(user.photoUrl).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "getOppositeUserInfo:onCancelled", databaseError.toException());
            }
        });
    }
}

package com.moviting.android.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.ui.activity.AdjustUserPreferenceActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EnrollFragment extends BaseFragment {

    private static final String TAG = "EnrollFragment";
    private static final int REFRESH_CODE = 1;

    private ImageButton adjustUserPreference;
    private Button enrollButton;
    private TextView dateText;
    private TextView movieText;

    private ArrayList<String> selectedDates;
    private ArrayList<String> selectedMovies;
    private String choosedGender;
    private boolean isEnrolled;

    private Long minPrefAge;
    private Long maxPrefAge;

    public EnrollFragment() {
    }

    public static EnrollFragment newInstance() {
        EnrollFragment fragment = new EnrollFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enroll, container, false);

        adjustUserPreference = (ImageButton) view.findViewById(R.id.adjust_user_preference);
        adjustUserPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AdjustUserPreferenceActivity.createIntent(getActivity(), choosedGender, selectedDates, selectedMovies, minPrefAge, maxPrefAge), REFRESH_CODE);
            }
        });

        enrollButton = (Button) view.findViewById(R.id.enroll_button);
        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAllFilledUp()) {
                    getBaseActivity().showProgressDialog();
                    if(isEnrolled) {
                        getBaseActivity().getFirebaseDatabaseReference()
                                .child("users").child(getBaseActivity().getUid())
                                .child("userStatus").setValue("Joined", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(), R.string.release_success_text, Toast.LENGTH_SHORT).show();
                                isEnrolled = false;
                                updateApplicationButton();
                                getBaseActivity().hideProgressDialog();
                            }
                        });
                    } else {
                        getBaseActivity().getFirebaseDatabaseReference()
                                .child("users").child(getBaseActivity().getUid())
                                .child("userStatus").setValue("Enrolled", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(), R.string.apply_success_text, Toast.LENGTH_SHORT).show();
                                isEnrolled = true;
                                updateApplicationButton();
                                getBaseActivity().hideProgressDialog();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), "연인 찾기 조건을 설정해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dateText = (TextView) view.findViewById(R.id.date);
        movieText = (TextView) view.findViewById(R.id.movies);
        initUserDataAndUI();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REFRESH_CODE ) {
            Log.d(TAG, "onActivityResult");
            initUserDataAndUI();
        }
    }

    private void initUserDataAndUI(){
        selectedDates = new ArrayList<>();
        selectedMovies = new ArrayList<>();
        choosedGender = "";
        isEnrolled = false;
        minPrefAge = Long.valueOf(0);
        maxPrefAge = Long.valueOf(0);

        getUserEnrollStatus();
        getUserPrefDate();
        getUserPrefGender();
        getUserPrefMovie();
        getUserMinPrefAge();
        getUserMaxPrefAge();
    }

    private boolean isAllFilledUp() {
        if(selectedDates.size() != 0 && selectedMovies.size() != 0 && !choosedGender.equals("") && minPrefAge != 0 && maxPrefAge != 0) {
            return true;
        } else {
            return false;
        }
    }

    private void getUserEnrollStatus() {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(getBaseActivity().getUid())
                .child("userStatus").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getValue().equals("Joined")) {
                        isEnrolled = false;
                    } else if (dataSnapshot.getValue().equals("Enrolled")){
                        isEnrolled = true;
                    }
                    Log.d(TAG, "isEnrolled: " + isEnrolled);
                } else {
                    Log.d(TAG, "No isEnrolled");
                }
                updateApplicationButton();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void updateApplicationButton() {
        if(isEnrolled) {
            enrollButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ripple_button_enrolled));
            enrollButton.setText(R.string.enrolled_button_text);
        } else {
            enrollButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ripple_button_not_enrolled));
            enrollButton.setText(R.string.enroll_button_text);
        }
    }

    private String readableDate(String date) {

        DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String result = null;
        try {
            Date dateObj = sdFormat.parse(date);
            cal.setTime(dateObj);

            result = String.valueOf(cal.get(Calendar.YEAR));
            result = result.concat(". " + String.valueOf(cal.get(Calendar.MONTH) + 1));
            result = result.concat(". " + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            result = result.concat(getResources().getStringArray(R.array.day_of_week)[cal.get(Calendar.DAY_OF_WEEK)]);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void getUserPrefDate() {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(getBaseActivity().getUid())
                .child("preferredDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int i = 0;
                    for( String select : ((ArrayList<String>)dataSnapshot.getValue())) {
                        selectedDates.add(select);
                        select = readableDate(select);
                        if(i == 0) {
                            dateText.setText(select);
                        } else {
                            dateText.append(", " + select );
                        }
                        Log.d(TAG, "selectedDates" + select);
                        i++;
                    }
                } else {
                    Log.d(TAG, "No selectedDates");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserPrefGender() {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(getBaseActivity().getUid())
                .child("preferredGender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    choosedGender = (String) dataSnapshot.getValue();
                    Log.d(TAG, "selectedGender" + choosedGender);
                } else {
                    Log.d(TAG, "No selectedGender");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserPrefMovie() {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(getBaseActivity().getUid())
                .child("preferredMovie").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    int i = 0;
                    for (String select : ((ArrayList<String>) dataSnapshot.getValue())) {
                        if(i == 0) {
                            movieText.setText(select);
                        } else {
                            movieText.append(", " + select );
                        }
                        selectedMovies.add(select);
                        Log.d(TAG, "selectedMovie: " + select);
                        i++;
                    }
                } else {
                    Log.d(TAG, "No selectedMovie");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserMinPrefAge() {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(getBaseActivity().getUid())
                .child("minPrefAge").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    minPrefAge = (Long)dataSnapshot.getValue();
                    Log.d(TAG, "minPrefAge" + minPrefAge);
                } else {
                    Log.d(TAG, "No selectedAge");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserMaxPrefAge() {
        getBaseActivity().getFirebaseDatabaseReference()
                .child("users").child(getBaseActivity().getUid())
                .child("maxPrefAge").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    maxPrefAge = (Long)dataSnapshot.getValue();
                    Log.d(TAG, "maxPrefAge" + maxPrefAge);
                } else {
                    Log.d(TAG, "No selectedAge");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
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

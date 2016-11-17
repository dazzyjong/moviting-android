package com.moviting.android.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class EnrollFragment extends BaseFragment {

    private SharedPreferences prefs = null;
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
        return new EnrollFragment();
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
                    showProgressDialog();
                    if(isEnrolled) {
                        getFirebaseDatabaseReference()
                                .child("users").child(getUid())
                                .child("userStatus").setValue("Disenrolled", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(), R.string.release_success_text, Toast.LENGTH_SHORT).show();
                                isEnrolled = false;
                                updateApplicationButton();
                                hideProgressDialog();
                            }
                        });
                    } else {
                        getFirebaseDatabaseReference()
                                .child("users").child(getUid())
                                .child("userStatus").setValue("Enrolled", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(), R.string.apply_success_text, Toast.LENGTH_SHORT).show();
                                isEnrolled = true;
                                updateApplicationButton();
                                hideProgressDialog();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.request_finding_opponent_setting, Toast.LENGTH_SHORT).show();
                }
            }
        });

        dateText = (TextView) view.findViewById(R.id.date);
        movieText = (TextView) view.findViewById(R.id.movies);
        initUserDataAndUI();

        if(getActivity() != null && isAdded()) {
            prefs = getActivity().getSharedPreferences("com.moviting.android", MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean("first_enroll", true);
            if(isFirstRun) {
                new ShowcaseView.Builder(getActivity())
                        .setTarget(new ViewTarget((view.findViewById(R.id.adjust_user_preference))))
                        .setContentTitle("인연 찾기 설정")
                        .setStyle(R.style.CustomShowcaseTheme3)
                        .setContentText("조건을 설정하여 특별한 인연을 만나보세요")
                        .hideOnTouchOutside()
                        .build();
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getBoolean("first_enroll", true)) {
            prefs.edit().putBoolean("first_enroll", false).apply();
        }
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
        minPrefAge = 0L;
        maxPrefAge = 0L;

        getUserEnrollStatus();
        getUserPrefDate();
        getUserPrefGender();
        getUserPrefMovie();
        getUserMinPrefAge();
        getUserMaxPrefAge();
    }

    private boolean isAllFilledUp() {
        return selectedDates.size() != 0 && selectedMovies.size() != 0 && !choosedGender.equals("") && minPrefAge != 0 && maxPrefAge != 0;
    }

    private void getUserEnrollStatus() {
        getFirebaseDatabaseReference()
                .child("users").child(getUid())
                .child("userStatus").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getValue().equals("Disenrolled")) {
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
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void updateApplicationButton() {
        if(isEnrolled) {
            if(getActivity() != null && isAdded()) {
                enrollButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ripple_button_enrolled));
                enrollButton.setText(R.string.enrolled_button_text);
            }
        } else {
            if(getActivity() != null && isAdded()) {
                enrollButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ripple_button_not_enrolled));
                enrollButton.setText(R.string.enroll_button_text);
            }
        }
    }

    private String readableDate(String date) {

        DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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
        getFirebaseDatabaseReference()
                .child("users").child(getUid())
                .child("preferredDate").orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && getActivity() != null && isAdded()){
                    int i = 0;
                    for(DataSnapshot dateSnapshot : dataSnapshot.getChildren() ) {
                        selectedDates.add((String)dateSnapshot.getValue());
                        String select = readableDate((String)dateSnapshot.getValue());
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
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserPrefGender() {
        getFirebaseDatabaseReference()
                .child("users").child(getUid())
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
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserPrefMovie() {
        getFirebaseDatabaseReference()
                .child("users").child(getUid())
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
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserMinPrefAge() {
        getFirebaseDatabaseReference()
                .child("users").child(getUid())
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
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void getUserMaxPrefAge() {
        getFirebaseDatabaseReference()
                .child("users").child(getUid())
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
                if(getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

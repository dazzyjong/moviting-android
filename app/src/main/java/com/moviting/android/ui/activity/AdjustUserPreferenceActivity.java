package com.moviting.android.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdjustUserPreferenceActivity extends BaseActivity {

    private static final String TAG = "AdjustActivity";
    private static final int SET_PREF_GENDER_VALUE = 1;
    private static final int SET_PREF_DATE_VALUE = 3;
    private static final int SET_PREF_MOVIE_VALUE = 4;
    private static final int SET_PREF_AGE_VALUE = 2;

    private long minAge = 19;
    private long maxAge = 50;

    private long prevMinAge;
    private long prevMaxAge;

    private ListView userPrefList;
    private ListViewAdapter userPrefListViewAdapter;
    private RangeBar mRangeBar;

    private String preferredGender;
    private ArrayList<String> preferredDate;
    private ArrayList<String> preferredMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_user_preference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferredGender = getIntent().getStringExtra("preferredGender");
        preferredDate = getIntent().getStringArrayListExtra("preferredDate");
        preferredMovie = getIntent().getStringArrayListExtra("preferredMovie");
        minAge = getIntent().getLongExtra("minPrefAge", 19);
        maxAge = getIntent().getLongExtra("maxPrefAge", 50);
        if(!getIntent().hasExtra("minPrefAge")) {
            updateSetting(true, minAge);
            updateSetting(false, maxAge);
        }

        Log.d(TAG, preferredGender + " / " + preferredDate + " / " + preferredMovie);

        userPrefList = (ListView) findViewById(R.id.user_pref_tab_list);

        userPrefListViewAdapter = new ListViewAdapter(this);
        userPrefList.setAdapter(userPrefListViewAdapter);

        String[] tags = new String[]{"성별", "나이 범위", "일자", "상영관", "영화"};

        for (String tag : tags) {
            if (tag.equals("상영관")) {
                userPrefListViewAdapter.addItem(tag, "서울 내 CGV");
            } else if (tag.equals("일자")) {
                userPrefListViewAdapter.addItem(tag, stringArrayListToString(preferredDate, true));
            } else if (tag.equals("성별")) {
                userPrefListViewAdapter.addItem(tag, setGenderValue(preferredGender));
            } else if (tag.equals("영화")) {
                userPrefListViewAdapter.addItem(tag, stringArrayListToString(preferredMovie, false));
            } else if (tag.equals("나이 범위")) {
                userPrefListViewAdapter.addItem(tag, minAge + " - " + maxAge);
            } else {
                userPrefListViewAdapter.addItem(tag, "");
            }
        }

        userPrefList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        startActivityForResult(ChoosePrefGenderActivity.createIntent(getBaseContext(), preferredGender), SET_PREF_GENDER_VALUE);
                        break;
                    case 2:
                        startActivityForResult(ChoosePrefDateActivity.createIntent(getBaseContext(), preferredDate), SET_PREF_DATE_VALUE);
                        break;
                    case 3:
                        Toast.makeText(getBaseContext(), R.string.enroll_location_warning_message, Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        startActivityForResult(ChoosePrefMovieActivity.createIntent(getBaseContext(), preferredMovie), SET_PREF_MOVIE_VALUE);
                        break;
                }
            }
        });
    }

    private String setGenderValue(String value) {
        if (value.equals("male")) {
            return "남성";
        } else if (value.equals("female")) {
            return "여성";
        } else if (value.equals("both")) {
            return "남성 및 여성";
        }
        return null;
    }

    private String stringArrayListToString(ArrayList<String> arrayList, boolean isDateList) {
        int i = 0;
        String result = "";
        for (String str : arrayList) {
            if (isDateList) {
                str = readableDate(str);
            }
            if (i == 0) {
                result = str;
            } else {
                result = result.concat(", " + str);
            }
            i++;
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SET_PREF_GENDER_VALUE:
                    userPrefListViewAdapter.updateItem(requestCode, data.getStringExtra("preferredGender"));
                    preferredGender = data.getStringExtra("preferredGender");
                    break;

                case SET_PREF_DATE_VALUE:
                    ArrayList<String> dates = data.getStringArrayListExtra("preferredDate");
                    userPrefListViewAdapter.updateItem(requestCode, stringArrayListToString(dates, true));
                    preferredDate = dates;
                    break;

                case SET_PREF_MOVIE_VALUE:
                    ArrayList<String> movies = data.getStringArrayListExtra("preferredMovie");
                    userPrefListViewAdapter.updateItem(requestCode, stringArrayListToString(movies, false));
                    preferredMovie = movies;
                    break;
            }
        }
    }

    private String readableDate(String date) {

        DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String result = null;
        try {
            Date dateObj = sdFormat.parse(date);
            cal.setTime(dateObj);

            result = String.valueOf(cal.get(Calendar.MONTH) + 1);
            result = result.concat(". " + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            result = result.concat(getResources().getStringArray(R.array.day_of_week)[cal.get(Calendar.DAY_OF_WEEK)]);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class ViewHolder {
        public TextView tag;
        public TextView value;
    }

    public class UserPrefRow {
        public String mTag;
        public String mValue;
    }


    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<UserPrefRow> mUserPrefRows = new ArrayList<>();

        private ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mUserPrefRows.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserPrefRows.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void addItem(String tag, String value) {
            UserPrefRow rowInfo = null;
            rowInfo = new UserPrefRow();

            rowInfo.mTag = tag;
            rowInfo.mValue = value;

            mUserPrefRows.add(rowInfo);
        }

        private void updateItem(int key, String value) {
            switch (key) {
                case SET_PREF_GENDER_VALUE:
                    mUserPrefRows.get(0).mValue = setGenderValue(value);
                    break;
                case SET_PREF_AGE_VALUE:
                    mUserPrefRows.get(1).mValue = value;
                    break;
                case SET_PREF_DATE_VALUE:
                    mUserPrefRows.get(2).mValue = value;
                    break;
                case SET_PREF_MOVIE_VALUE:
                    mUserPrefRows.get(4).mValue = value;
                    break;
            }
            dataChange();
        }

        private void dataChange() {
            userPrefListViewAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user_pref_item, null);

                holder.tag = (TextView) convertView.findViewById(R.id.tag);
                holder.value = (TextView) convertView.findViewById(R.id.value);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            UserPrefRow mData = mUserPrefRows.get(position);

            holder.tag.setText(mData.mTag);
            holder.value.setText(mData.mValue);

            if (position == 1) {
                RangeBar rangeBar = (RangeBar) convertView.findViewById(R.id.rangebar);
                rangeBar.setVisibility(View.VISIBLE);
                setRangebar(rangeBar);
                convertView.findViewById(R.id.arrow).setVisibility(View.GONE);
                convertView.setFocusable(true);
                convertView.setClickable(false);
            }

            return convertView;
        }

        void setRangebar(RangeBar rangeBar) {
            mRangeBar = rangeBar;
            mRangeBar.setRangePinsByValue(minAge, maxAge);
            mRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
                @Override
                public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                    Log.d(TAG, "onRangeChangeListener: " + leftPinValue + " / " + rightPinValue);
                    minAge = Integer.valueOf(leftPinValue);
                    maxAge = Integer.valueOf(rightPinValue);
                    if (minAge < 19) {
                        rangeBar.setRangePinsByValue(19, maxAge);
                        minAge = 19;
                    }
                    if (maxAge > 50) {
                        rangeBar.setRangePinsByValue(minAge, 50);
                        maxAge = 50;
                    }
                }
            });

            mRangeBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.d("TouchTest", "Touch down");
                        prevMinAge = minAge;
                        prevMaxAge = maxAge;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d("TouchTest", "Touch up");
                        updateItem(SET_PREF_AGE_VALUE, minAge + " - " + maxAge);

                        if(minAge != prevMinAge) {
                            updateSetting(true, minAge);
                        } else if(maxAge != prevMaxAge) {
                            updateSetting(false, maxAge);
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void updateSetting(boolean isMin, long value) {
        showProgressDialog();
        if(isMin) {
            getFirebaseDatabaseReference().child("users").child(getUid()).child("minPrefAge").setValue(value, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    hideProgressDialog();
                }
            });
        } else {
            getFirebaseDatabaseReference().child("users").child(getUid()).child("maxPrefAge").setValue(value, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    hideProgressDialog();
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                AdjustUserPreferenceActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public static Intent createIntent(Context context, String
            gender, ArrayList<String> dates, ArrayList<String> movies, long minPrefAge, long maxPrefAge) {
        Intent in = new Intent();
        in.putExtra("preferredGender", gender);
        in.putStringArrayListExtra("preferredDate", dates);
        in.putStringArrayListExtra("preferredMovie", movies);
        if(minPrefAge != 0 && maxPrefAge != 0) {
            in.putExtra("minPrefAge", minPrefAge);
            in.putExtra("maxPrefAge", maxPrefAge);
        }
        in.setClass(context, AdjustUserPreferenceActivity.class);
        return in;
    }
}

package com.moviting.android.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.moviting.android.util.DatabaseHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private static User userInstance;

    private enum UserStatus {
        Joined, Enrolled
    }

    public String name;
    public String email;
    public String photoUrl;
    private UserStatus userStatus;

    public String gender;
    public String birthday;
    public String favoriteMovie;
    public String school;
    public String work;
    public String height;
    public String introduce;
    public int myAge;
    public int minPrefAge;
    public int maxPrefAge;
    public String preferredGender;

    private static DatabaseReference mRef;
    private static ChildEventListener mChildEventListener;

    private User(){
    }

    private User(String uid, String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.userStatus = UserStatus.valueOf("Joined");
        databaseListener(uid);
    }

    @Exclude
    private void databaseListener(final String uid) {
        Log.d("User", "databaseListner");
        mRef = DatabaseHelper.getInstance().getReference();
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("User", "onChildAdded:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("User", "onChildChanged:" + dataSnapshot.getKey());
                updateMember(dataSnapshot.getKey(), dataSnapshot.getValue());

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("User", "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("User", "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("User", "postComments:onCancelled", databaseError.toException());
            }
        };
        mRef.child("users").child(uid).addChildEventListener(mChildEventListener);
    }

    @Exclude
    public void transferBirthYearToMyAge(int birthYear) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        this.myAge = currentYear - birthYear + 1;
    }

    public void setMyAge(int age) {
        this.myAge = age;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public void setFavoriteMovie(String favoriteMovie) {
        this.favoriteMovie = favoriteMovie;
    }
    public void setSchool(String school) {
        this.school = school;
    }
    public void setWork(String work) {
        this.work = work;
    }
    public void setHeight(String height) {
        this.height = height;
    }
    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }
    public void setMinPrefAge(int age) {
        this.minPrefAge = age;
    }
    public void setMaxPrefAge(int age) {
        this.maxPrefAge = age;
    }
    public void setPrefGender(String gender) {
        this.preferredGender = gender;
    }

    @Exclude
    public static User constructUserInstance(String uid, String name, String email, String photoUrl) throws IllegalStateException{
        if(userInstance != null) {
            return userInstance;
        } else {
            userInstance = new User(uid, name, email, photoUrl);
        }
        return userInstance;
    }

    @Exclude
    public static User getUserInstance() throws IllegalStateException{
        if(userInstance == null) {
            throw new IllegalStateException("Before getUserInstance, please call constructUserInstance first");
        }
        return userInstance;
    }

    @Exclude
    public static void destructUserInstance() {
        userInstance = null;
        mRef.removeEventListener(mChildEventListener);
    }

    @Exclude
    public User copyFrom(User user, String uid) {
        if(userInstance == null) {
            userInstance = new User();
        }

        userInstance.name = user.name != null ? user.name : null;
        userInstance.email = user.email != null ? user.email : null;
        userInstance.photoUrl = user.photoUrl != null ? user.photoUrl : null;
        userInstance.userStatus = user.userStatus != null ? user.userStatus : null;
        userInstance.gender = user.gender != null ? user.gender : null;
        userInstance.birthday = user.birthday != null ? user.birthday : null;
        userInstance.favoriteMovie = user.favoriteMovie != null ? user.favoriteMovie : null;
        userInstance.school = user.school != null ? user.school : null;
        userInstance.work = user.work != null ? user.work : null;
        userInstance.height = user.height != null ? user.height : null;
        userInstance.introduce = user.introduce != null ? user.introduce : null;
        userInstance.myAge = user.myAge != 0 ? user.myAge : 0;
        userInstance.minPrefAge = user.minPrefAge != 0 ? user.minPrefAge : 0;
        userInstance.maxPrefAge = user.maxPrefAge != 0 ? user.maxPrefAge : 0;
        userInstance.preferredGender = user.preferredGender != null ? user.preferredGender : null;

        databaseListener(uid);

        return userInstance;
    }

    @Exclude
    public UserStatus getUserStatusAsEnum() {
        return userStatus;
    }

    public String getUserStatus() {
        if(userStatus == null) {
            return null;
        } else {
            return userStatus.name();
        }
    }

    public void setUserStatus(@NonNull String userStatusString) {
        this.userStatus = UserStatus.valueOf(userStatusString);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("email", email);
        result.put("photoUrl", photoUrl);
        result.put("userStatus", userStatus.name());
        if(gender != null) {
            result.put("gender", gender);
        } else {
            result.put("gender", "");
        }
        if(birthday != null) {
            result.put("birthday", birthday);
        } else {
            result.put("birthday", "");
        }
        if(favoriteMovie != null) {
            result.put("favoriteMovie", favoriteMovie);
        } else {
            result.put("favoriteMovie", "");
        }
        if(school != null) {
            result.put("school", school);
        } else {
            result.put("school", "");
        }
        if(work != null) {
            result.put("work", work);
        } else {
            result.put("work", "");
        }
        if(height != null) {
            result.put("height", height);
        } else {
            result.put("height", "");
        }
        if(introduce != null) {
            result.put("introduce", introduce);
        } else {
            result.put("introduce", "");
        }
        if(myAge != 0) {
            result.put("myAge", myAge);
        } else {
            result.put("myAge", 0);
        }

        return result;
    }

    @Exclude
    public boolean isUserFormFilled() {

        if(name.equals("")) {
            return false;
        }
        if(photoUrl.equals("")) {
            return false;
        }
        if(gender.equals("")) {
            return false;
        }
        if(birthday.equals("")) {
            return false;
        }
        if(favoriteMovie.equals("")) {
            return false;
        }
        if(school.equals("")) {
            return false;
        }
        if(work.equals("")) {
            return false;
        }
        if(height.equals("")) {
            return false;
        }

        return true;
    }

    @Exclude
    private void updateMember(String key, Object value) {
        if(key.equals("birthday")) {
            this.birthday = (String)value;
        }
        if(key.equals("email")) {
            this.email = (String)value;
        }
        if(key.equals("favoriteMovie")) {
            this.favoriteMovie = (String)value;
        }
        if(key.equals("gender")) {
            this.gender = (String)value;
        }
        if(key.equals("height")) {
            this.height = (String)value;
        }
        if(key.equals("introduce")) {
            this.introduce = (String)value;
        }
        if(key.equals("maxPrefAge")) {
            this.maxPrefAge = ((Long)value).intValue();
        }
        if(key.equals("minPrefAge")) {
            this.minPrefAge = ((Long)value).intValue();
        }
        if(key.equals("myAge")) {
            this.minPrefAge = ((Long)value).intValue();
        }
        if(key.equals("name")) {
            this.name = (String)value;
        }
        if(key.equals("photoUrl")) {
            this.photoUrl = (String)value;
        }
        if(key.equals("preferredGender")) {
            this.preferredGender = (String)value;
        }
        if(key.equals("school")) {
            this.school = (String)value;
        }
        if(key.equals("userStatus")) {
            this.userStatus = UserStatus.valueOf((String)value);
        }
        if(key.equals("work")) {
            this.work = (String)value;
        }
    }
}

package com.moviting.android.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private static User userInstance;

    public enum UserStatus {
        Joined, Applied,  Matched
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
    public int minPrefAge;
    public int maxPrefAge;

    private User(){

    }

    private User(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.userStatus = UserStatus.valueOf("Joined");
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

    @Exclude
    public static User constructUserInstance(String name, String email, String photoUrl) throws IllegalStateException{
        if(userInstance != null) {
            return userInstance;
        } else {
            userInstance = new User(name, email, photoUrl);
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
    }

    @Exclude
    public static User copyFrom(User user) {
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
        userInstance.minPrefAge = user.minPrefAge != 0 ? user.minPrefAge : 0;
        userInstance.maxPrefAge = user.maxPrefAge != 0 ? user.maxPrefAge : 0;

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

        return result;
    }

    public boolean isUserFormFilled() {
        boolean result = true;


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

        return result;
    }
}

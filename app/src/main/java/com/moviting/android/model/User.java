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
        Joined, Applied, Proposed, Matched
    }

    public String name;
    public String email;
    public String photoUrl;

    public String gender;
    public String birthday;
    private UserStatus userStatus;

    // to verify user more(optional)
    public String school;
    public String org;

    // optional
    public String height;

    private User(){

    }

    private User(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.userStatus = UserStatus.valueOf("Joined");
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    // TODO: need to be updated from ProfileFragment
    public void setHeight(String height) {
        this.height = height;
    }
    public void setSchool(String school) {
        this.school = school;
    }
    public void setOrg(String org) {
        this.org = org;
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
            userInstance.name = user.name != null ? user.name : null;
            userInstance.email = user.email != null ? user.email : null;
            userInstance.photoUrl = user.photoUrl != null ? user.photoUrl : null;
            userInstance.gender = user.gender != null ? user.gender : null;
            userInstance.birthday = user.birthday != null ? user.birthday : null;
            userInstance.userStatus = user.userStatus != null ? user.userStatus : null;
            userInstance.school = user.school != null ? user.school : null;
            userInstance.org = user.org != null ? user.org : null;
            userInstance.height = user.height != null ? user.height : null;
        }
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
        if(school != null) {
            result.put("school", school);
        } else {
            result.put("school", "");
        }
        if(org != null) {
            result.put("org", org);
        } else {
            result.put("org", "");
        }
        if(height != null) {
            result.put("height", height);
        } else {
            result.put("height", "");
        }

        return result;
    }
}

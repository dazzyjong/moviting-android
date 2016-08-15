package com.moviting.android.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public enum UserStatus {
        Joined, Applied, Proposed, Matched
    }

    public String name;
    public String email;
    public String photoUrl;

    public String gender;
    public String birthday;
    public String height;
    public String school;
    public String org;
    private UserStatus userStatus;

    public User(){

    }

    public User(String name, String email, String photoUrl) {
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
}

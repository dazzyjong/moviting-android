package com.moviting.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User implements Parcelable {

    public String userStatus;
    public String name;
    public String email;
    public String photoUrl;

    public String gender;
    public String birthday;
    public String favoriteMovie;
    public String school;
    public String work;
    public String height;
    public int myAge;
    public int minPrefAge;
    public int maxPrefAge;
    public String preferredGender;

    public User(){
    }

    public User(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.userStatus = "Joined";
    }

    private User(Parcel in) {
        readFromParcel(in);
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
    public void setMinPrefAge(int age) {
        this.minPrefAge = age;
    }
    public void setMaxPrefAge(int age) {
        this.maxPrefAge = age;
    }
    public void setPrefGender(String gender) {
        this.preferredGender = gender;
    }
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("email", email);
        result.put("photoUrl", photoUrl);
        result.put("userStatus", userStatus);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userStatus);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(photoUrl);
        parcel.writeString(gender);
        parcel.writeString(birthday);
        parcel.writeString(favoriteMovie);
        parcel.writeString(school);
        parcel.writeString(work);
        parcel.writeString(height);
        parcel.writeInt(myAge);
        parcel.writeInt(minPrefAge);
        parcel.writeInt(maxPrefAge);
        parcel.writeString(preferredGender);

    }

    @Exclude
    private void readFromParcel(Parcel in){
        userStatus      = in.readString();
        name            = in.readString();
        email           = in.readString();
        photoUrl        = in.readString();
        gender          = in.readString();
        birthday        = in.readString();
        favoriteMovie   = in.readString();
        school          = in.readString();
        work            = in.readString();
        height          = in.readString();
        myAge           = in.readInt();
        minPrefAge      = in.readInt();
        maxPrefAge      = in.readInt();
        preferredGender = in.readString();
    }

    @Exclude
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}

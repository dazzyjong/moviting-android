package com.moviting.android.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jongseonglee on 10/26/16.
 */

public class UserPreference implements Serializable{
    public UserPreference() {
        preferredMovie = new ArrayList<>();
        preferredDate = new ArrayList<>();
    }
    public ArrayList<String> preferredMovie;
    public ArrayList<String> preferredDate;
}

package com.moviting.android.model;

import java.io.Serializable;

/**
 * Created by jongseonglee on 10/9/16.
 */

public class MatchInfo implements Serializable {
    public String matchUid;
    public String opponentUid;
    public String opponentName;
    public String opponentPhotoPath;
    public String opponentType;
    public String opponentGender;
    public String myType;
    public String myGender;
    public boolean opponentPayment;
    public boolean myPayment;

    public MatchInfo() {
        matchUid = "";
        opponentName = "";
        opponentName = "";
        opponentPhotoPath = "";
        opponentType = "";
        opponentGender = "";
        myType = "";
        myGender = "";
        opponentPayment = false;
        myPayment = false;
    }
}

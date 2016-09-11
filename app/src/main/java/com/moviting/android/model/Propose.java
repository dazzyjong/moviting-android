package com.moviting.android.model;

import android.support.annotation.NonNull;

/**
 * Created by jongseonglee on 9/6/16.
 */

public class Propose {
    private String mUid;
    private ProposeStatus mStatus;

    public Propose() {

    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getStatus() {
        if(mStatus == null){
            return null;
        } else {
            return mStatus.name();
        }
    }

    public void setStatus(@NonNull String mStatusString) {
        this.mStatus = ProposeStatus.valueOf(mStatusString);
    }

    public enum ProposeStatus {
        Proposed, Like,  Dislike
    }
}

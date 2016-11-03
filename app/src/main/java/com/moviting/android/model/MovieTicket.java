package com.moviting.android.model;

import java.io.Serializable;

/**
 * Created by jongseonglee on 10/19/16.
 */

public class MovieTicket implements Serializable {
    public String ticketId;
    public String expirationDate;
    public boolean screen;

    public MovieTicket(String ticketId, String expirationDate, boolean screen) {
        this.ticketId = ticketId;
        this.expirationDate = expirationDate;
        this.screen = screen;
    }
}

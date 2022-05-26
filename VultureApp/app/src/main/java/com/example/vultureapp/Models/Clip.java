package com.example.vultureapp.Models;

public class Clip {

    private long id;
    private String dateTime;

    public Clip(long id, String dateTime) {
        this.id = id;
        this.dateTime = dateTime;
    }

    public long getId() {
        return id;
    }

    public String getDateTime() {
        return dateTime;
    }
}

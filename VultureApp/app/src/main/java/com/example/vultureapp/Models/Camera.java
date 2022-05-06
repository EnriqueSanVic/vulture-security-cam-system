package com.example.vultureapp.Models;

public class Camera {

    private int id;
    private String name;

    public Camera(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

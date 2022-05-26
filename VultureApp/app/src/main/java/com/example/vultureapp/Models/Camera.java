package com.example.vultureapp.Models;

public class Camera {

    private long id;
    private String name;

    public Camera(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Camera{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

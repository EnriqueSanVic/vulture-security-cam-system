package com.example.vultureapp.Models;

public class DataSingleton {


    public Camera[] camSet = {};

    private static DataSingleton instance;


    private DataSingleton() {

    }

    public static DataSingleton getInstance(){
        if(instance == null){
            instance = new DataSingleton();
        }

        return instance;
    }

    public void setCamSet(Camera[] camSet) {
        this.camSet = camSet;
    }

    public Camera findCameraId(long id){
        for (int j = 0; j < camSet.length; j++) {
            if (camSet[j].getId() == id) return camSet[j];
        }

        return null;
    }
}

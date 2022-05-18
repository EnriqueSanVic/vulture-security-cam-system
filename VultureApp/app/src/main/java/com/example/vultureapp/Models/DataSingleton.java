package com.example.vultureapp.Models;

public class DataSingleton {


    public Camera[] camSet = {
            new Camera(1, "Salón"),
            new Camera(2, "Cocina"),
            new Camera(3, "Baño"),
            new Camera(4, "Jardín"),
            new Camera(5, "Puerta trasera"),
            new Camera(6, "Garaje"),
            new Camera(7, "Puerta de entrada"),
            new Camera(8, "Habitaión bebé"),
            new Camera(9, "Habitación izquierda"),
            new Camera(10, "Habitación derecha"),
            new Camera(11, "Habitación buardilla"),
            new Camera(12, "Habitación grande"),
            new Camera(13, "Sótano"),
            new Camera(14, "Tejado"),
            new Camera(15, "Exterior izquierda"),
            new Camera(16, "Exterior derecha")
    };


    private static DataSingleton instance;


    private DataSingleton() {

    }

    public static DataSingleton getInstance(){
        if(instance == null){
            instance = new DataSingleton();
        }

        return instance;
    }

    public Camera findCameraId(int id){
        for (int j = 0; j < camSet.length; j++) {
            if (camSet[j].getId() == id) return camSet[j];
        }

        return null;
    }
}

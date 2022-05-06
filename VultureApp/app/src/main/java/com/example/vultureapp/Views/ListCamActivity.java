package com.example.vultureapp.Views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.vultureapp.Adapters.ListCamAdapter;
import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.R;

public class ListCamActivity extends AppCompatActivity {

    private RecyclerView camList;
    private ListCamAdapter listAdapter;

    private final Camera[] camSet = {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cam);

        camList = findViewById(R.id.camList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        camList.setLayoutManager(layoutManager);

        listAdapter = new ListCamAdapter(camSet);

        camList.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();
    }
}
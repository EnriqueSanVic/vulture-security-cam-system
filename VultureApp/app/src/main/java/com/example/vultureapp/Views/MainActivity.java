package com.example.vultureapp.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.vultureapp.Controllers.MainController;
import com.example.vultureapp.R;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;

    private MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        confAppBar();

        controller = new MainController(this);

        findElements();
    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    private void findElements() {

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(controller);

    }


    public void goToCamListView(){

        Intent intent = new Intent(this, ListCamActivity.class);

        startActivity(intent);

    }

    public void goToCamView(){

        Intent intent = new Intent(this, CamActivity.class);

        startActivity(intent);

    }
}
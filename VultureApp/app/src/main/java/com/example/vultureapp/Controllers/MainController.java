package com.example.vultureapp.Controllers;

import android.view.View;

import com.example.vultureapp.Views.MainActivity;

public class MainController implements View.OnClickListener {

    private MainActivity view;

    public MainController(MainActivity view) {
        this.view = view;
    }

    @Override
    public void onClick(View view) {

        this.view.goToCamListView();

    }
}

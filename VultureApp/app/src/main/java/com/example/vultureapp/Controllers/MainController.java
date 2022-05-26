package com.example.vultureapp.Controllers;

import android.view.View;

import com.example.vultureapp.Callbacks.ThreadCallBack;
import com.example.vultureapp.Models.Response;
import com.example.vultureapp.Threads.APIThread;
import com.example.vultureapp.Views.MainActivity;

public class MainController implements View.OnClickListener {

    private MainActivity view;
    private APIThread api;

    public MainController(MainActivity view) {
        this.view = view;
        this.api = APIThread.getInstance();
    }

    @Override
    public void onClick(View comp) {

        api.setCredentials(view.getMail(), view.getPassword());

        api.setNextCallback(new ThreadCallBack() {
            @Override
            public void callBack(Response status) {
                view.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(status.isStatus()){
                            APIThread.getInstance().setAuth(true);
                            view.goToCamListView();
                        }else{
                            view.clearPassword();
                            view.showNoAuthMessage();
                        }
                    }
                });
            }
        });

        api.sendRequest();

    }
}

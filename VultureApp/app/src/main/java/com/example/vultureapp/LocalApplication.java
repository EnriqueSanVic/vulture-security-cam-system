package com.example.vultureapp;

import android.app.Application;

import com.example.vultureapp.Threads.APIThread;


public class LocalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        APIThread.getInstance().start();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        APIThread.getInstance().closeConnection();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

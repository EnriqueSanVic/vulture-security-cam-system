package com.example.vultureapp.Controllers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.vultureapp.Callbacks.FrameCallBack;
import com.example.vultureapp.Callbacks.ThreadCallBack;
import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.Models.DataSingleton;
import com.example.vultureapp.Models.Request;
import com.example.vultureapp.Models.Response;
import com.example.vultureapp.Threads.APIThread;
import com.example.vultureapp.Views.CamActivity;

public class CamController {

    private CamActivity view;

    private Camera camera;

    private Bitmap frame;

    public CamController(CamActivity view) {
        this.view = view;

    }

    public boolean loadCamera(long camId){
        camera = DataSingleton.getInstance().findCameraId(camId);
        return (camera == null);
    }

    public void conf() {

        view.setHeaderText(camera.getId() + " - " + camera.getName());

        APIThread.getInstance().setNextRequest(new Request(Request.STREAMING_TRANSMISSION_REQUEST_COMMAND, null, null, camera.getId(), 0));

        APIThread.getInstance().setNextCallback(new ThreadCallBack() {
            @Override
            public void callBack(Response response) {

                if(response.isStatus()){
                    startTransmission();
                }else{
                    serverNotProvideTransmission();
                }


            }
        });

        APIThread.getInstance().sendRequest();

    }

    private void serverNotProvideTransmission() {
    }

    private void startTransmission() {

        //the next sendRequest is for init lease the streaming in the thread
        APIThread.getInstance().setNextInitStreaming(true);

        //frames callback
        APIThread.getInstance().setNextFrameCallBack(new FrameCallBack() {
            @Override
            public void callBack(byte[] frameBytes, int len) {

                view.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        frame = BitmapFactory.decodeByteArray(frameBytes, 0, len);
                        view.setFrame(frame);
                    }
                });

            }
        });


        APIThread.getInstance().sendRequest();

    }



    public void shutdownStreaming() {

        APIThread.getInstance().shutdownStreaming();

    }
}

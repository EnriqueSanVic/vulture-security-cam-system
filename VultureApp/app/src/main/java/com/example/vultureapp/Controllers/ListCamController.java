package com.example.vultureapp.Controllers;

import com.example.vultureapp.Callbacks.ThreadCallBack;
import com.example.vultureapp.Models.DataSingleton;
import com.example.vultureapp.Models.Request;
import com.example.vultureapp.Models.Response;
import com.example.vultureapp.Threads.APIThread;
import com.example.vultureapp.Views.ListCamActivity;

public class ListCamController {

    private ListCamActivity view;

    public ListCamController(ListCamActivity view) {
        this.view = view;
    }

    public void loadData(){

        ThreadCallBack callback = new ThreadCallBack() {
            @Override
            public void callBack(Response response) {

                //set the camera list in the data singleton
                DataSingleton.getInstance().setCamSet(response.getMutateListOfCam());

                view.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.renderCamList();
                    }
                });

            }
        };

        APIThread.getInstance().setNextRequest(new Request(Request.LIST_CAMS_REQUEST_COMMAND, null, null, 0, 0));
        APIThread.getInstance().setNextCallback(callback);
        APIThread.getInstance().sendRequest();


    }
}

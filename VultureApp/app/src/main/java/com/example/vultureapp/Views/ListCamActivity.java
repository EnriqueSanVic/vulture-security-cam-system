package com.example.vultureapp.Views;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.vultureapp.Adapters.ListCamAdapter;
import com.example.vultureapp.Callbacks.ThreadCallBack;
import com.example.vultureapp.Controllers.ListCamController;
import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.Models.Clip;
import com.example.vultureapp.Models.DataSingleton;
import com.example.vultureapp.Models.Request;
import com.example.vultureapp.Models.Response;
import com.example.vultureapp.R;
import com.example.vultureapp.Threads.APIThread;

import java.util.ArrayList;


public class ListCamActivity extends AppCompatActivity {

    private ListCamController controller;
    private RecyclerView camList;
    private ListCamAdapter listAdapter;

    public static String CAM_ID_EXTRA = "camera";
    public static String CLIP_ID_EXTRA = "clip";

    public ListCamActivity viewContext;

    private Clip[] actualClipList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cam);

        controller = new ListCamController(this);

        confAppBar();

        viewContext = this;

        camList = findViewById(R.id.camList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        camList.setLayoutManager(layoutManager);

        controller.loadData();
    }

    public void renderCamList(){
        listAdapter = new ListCamAdapter(DataSingleton.getInstance().camSet, this);
        camList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    public void goToCamActivity(Camera cam){
        Intent intent = new Intent(ListCamActivity.this, CamActivity.class);
        intent.putExtra(CAM_ID_EXTRA, cam.getId());
        Log.d("Intent CamActivity", "GO");
        startActivity(intent);
    }

    public void createRecListMessageDialog(Camera cam){
        //realizar peticion para obtener grabaciones

        Request request = new Request(Request.LIST_CLIPS_REQUEST_COMMAND, null, null, cam.getId(), 0);

        APIThread.getInstance().setNextRequest(request);

        APIThread.getInstance().setNextCallback(new ThreadCallBack() {
            @Override
            public void callBack(Response response) {

                actualClipList = response.getMutateListOfClips();

                generateRecListMessageDialog();
            }
        });


        APIThread.getInstance().sendRequest();


    }

    private void generateRecListMessageDialog() {

        String[] options = generateOptionList();

        viewContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(viewContext);
                builder.setTitle("Choose a clip");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        viewContext.goToClipViewer(actualClipList[which]);

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private String[] generateOptionList() {

        String[] options = new String[actualClipList.length];

        for (int i = 0; i < actualClipList.length; i++) {
            options[i] = actualClipList[i].getDateTime();
        }

        return options;
    }

    private void goToClipViewer(Clip clip) {

        Intent intent = new Intent(this, ClipView.class);

        intent.putExtra(CLIP_ID_EXTRA, clip.getId());

        startActivity(intent);
    }
}
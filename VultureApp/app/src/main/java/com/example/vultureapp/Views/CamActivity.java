package com.example.vultureapp.Views;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vultureapp.Controllers.CamController;
import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.Models.DataSingleton;
import com.example.vultureapp.R;

//pensar en implementar: https://exoplayer.dev/ui-components.html
public class CamActivity extends AppCompatActivity {

    private CamController controller;

    private TextView labCamName;
    private ImageView imageLayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        controller = new CamController(this);

        confAppBar();
        confElements();

        Bundle extras = getIntent().getExtras();
        String userName;

        boolean initError = false;

        if (extras != null) {
            long camId = extras.getLong(ListCamActivity.CAM_ID_EXTRA);
            initError = controller.loadCamera(camId);
        }else{
            initError = true;
        }

        if(initError){
            finish();
        }

        controller.conf();

    }

    public void setHeaderText(String header){
        labCamName.setText(header);
    }

    private void confElements() {

        labCamName = findViewById(R.id.labCamName);
        imageLayer = findViewById(R.id.imageLayer);

    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    public void setFrame(Bitmap frame){
        imageLayer.setImageBitmap(frame);
    }




}
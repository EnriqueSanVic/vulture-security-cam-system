package com.example.vultureapp.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.Models.DataSingleton;
import com.example.vultureapp.R;

//pensar en implementar: https://exoplayer.dev/ui-components.html
public class CamActivity extends AppCompatActivity {

    private TextView labCamName;

    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        confAppBar();
        confElements();

        Bundle extras = getIntent().getExtras();
        String userName;

        boolean initError = false;

        if (extras != null) {
            int camId = extras.getInt(ListCamActivity.CAM_ID_EXTRA);
            camera = DataSingleton.getInstance().findCameraId(camId);

            if(camera == null){
                initError = true;
            }

        }else{
            initError = true;
        }

        if(initError){
            finish();
        }

        labCamName.setText(camera.getId() + " - " + camera.getName());

    }

    private void confElements() {

        labCamName = findViewById(R.id.labCamName);

    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }
}
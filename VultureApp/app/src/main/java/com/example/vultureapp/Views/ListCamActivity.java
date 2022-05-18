package com.example.vultureapp.Views;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.vultureapp.Adapters.ListCamAdapter;
import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.Models.DataSingleton;
import com.example.vultureapp.R;


public class ListCamActivity extends AppCompatActivity {

    private RecyclerView camList;
    private ListCamAdapter listAdapter;

    public static String CAM_ID_EXTRA = "camera";

    public ListCamActivity viewContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cam);

        confAppBar();

        viewContext = this;

        camList = findViewById(R.id.camList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        camList.setLayoutManager(layoutManager);

        listAdapter = new ListCamAdapter(DataSingleton.getInstance().camSet, this);

        camList.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();
    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    public void goToCamActivity(Camera cam){
        Intent intent = new Intent(this, CamActivity.class);
        intent.putExtra(CAM_ID_EXTRA, cam.getId());
        startActivity(intent);
    }

    public void createRecListMessageDialog(Camera cam){
        //realizar peticion para obtener grabaciones
        String[] options = {"Clip 1", "Clip 2", "Clip 3", "Clip 4", "Clip 5", "Clip 6", "Clip 7", "Clip 8", "Clip 9"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a clip");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                viewContext.goToClipViewer();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToClipViewer() {

        Intent intent = new Intent(this, ClipView.class);

        startActivity(intent);
    }
}
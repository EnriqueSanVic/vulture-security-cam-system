package com.example.vultureapp.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.example.vultureapp.Callbacks.ThreadCallBack;
import com.example.vultureapp.Models.Request;
import com.example.vultureapp.Models.Response;
import com.example.vultureapp.R;
import com.example.vultureapp.Threads.APIThread;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClipView extends AppCompatActivity {

    private StyledPlayerView clipViewer;

    private ExoPlayer player;

    private ClipView ownView;

    private int clipIdSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_view);

        ownView = this;

        confAppBar();
        clipViewer = findViewById(R.id.videoViewer);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            clipIdSelected = (int) bundle.getLong(ListCamActivity.CLIP_ID_EXTRA);
        }

        downloadClipData();

    }

    private void downloadClipData() {

        Request request = new Request(Request.CLIP_DOWNLOAD_REQUEST_COMMAND, null, null, 0, clipIdSelected);

        APIThread.getInstance().setNextRequest(request);

        //antes de esta callback el hilo de la api ya ha guardado el fichero descargado en la ruta seleccionada
        APIThread.getInstance().setNextCallback(new ThreadCallBack() {
            @Override
            public void callBack(Response response) {
                //if the status is true
                if(response.isStatus()){
                    //read the n bytes of socket
                    APIThread.getInstance().setFileHandler(ownView);
                    APIThread.getInstance().readFile(response.getnBytesOfClip());
                }
            }
        });

        APIThread.getInstance().sendRequest();

    }

    public synchronized void handleFile(byte[] bytes){

        try {

            File outputDir = ownView.getCacheDir(); // context being the Activity pointer
            File outputFile = File.createTempFile("temClip", ".mp4", outputDir);

            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(bytes);
            fos.close();

            System.out.println("Ruta : " + outputFile.getAbsolutePath());

            ownView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    renderVideoViewer(Uri.parse(outputFile.getAbsolutePath()));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    private synchronized void renderVideoViewer(Uri uri){
        player = new ExoPlayer.Builder(this).build();
        clipViewer.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        try {

        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }


}
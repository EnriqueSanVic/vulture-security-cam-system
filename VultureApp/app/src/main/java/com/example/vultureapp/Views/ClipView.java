package com.example.vultureapp.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.example.vultureapp.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.io.InputStream;

public class ClipView extends AppCompatActivity {

    private StyledPlayerView clipViewer;

    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_view);

        confAppBar();

        clipViewer = findViewById(R.id.videoViewer);

        player = new ExoPlayer.Builder(this).build();
// Attach player to the view.
        clipViewer.setPlayer(player);
// Set the media item to be played.
        player.setMediaItem(getClip());
// Prepare the player.
        player.prepare();

    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    private MediaItem getClip(){

        //String path = "android:resource://" + getPackageName() + "/" + R.raw.video3;
        Uri url = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video3);
        //InputStream inputStream  = getResources().openRawResource(R.raw.video3);

        return MediaItem.fromUri(url);
    }
}
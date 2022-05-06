package Views;

import Views.Controllers.CamViewControler;
import Views.Controllers.StreamingViewProcessor;

import javax.swing.*;
import java.awt.*;

public class CamView extends JFrame {


    private final int WIDTH = 1300, HEIGHT = 780;
    private final int WIDTH_STREAMING_AREA= 640, HEIGHT_STREAMING_AREA = 480;
    private final int X_STREAMING_AREA = 500, Y_STREAMING_AREA = 50;
    private final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;

    private CamViewControler controller;
    private StreamingViewProcessor streamingViewProcessor;

    private Canvas streamingArea;

    private static CamView singelton = null;

    public static CamView getInstance(){

        if(singelton == null){
            singelton = new CamView();
        }

        return singelton;
    }

    private CamView(){

        super();

        this.setSize(WIDTH, HEIGHT);

        this.setBackground(BACKGROUND_COLOR);

        this.setLayout(null);

        confStreamingArea();

        this.add(streamingArea);

        this.setVisible(true);

        streamingViewProcessor = new StreamingViewProcessor(streamingArea);

        controller = new CamViewControler(this, streamingViewProcessor);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                streamingViewProcessor.start();
            }
        });

    }

    private void confStreamingArea() {

        streamingArea = new Canvas();

        streamingArea.setSize(WIDTH_STREAMING_AREA, HEIGHT_STREAMING_AREA);

        streamingArea.setLocation(X_STREAMING_AREA, Y_STREAMING_AREA);

    }


    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public int getWIDTH_STREAMING_AREA() {
        return WIDTH_STREAMING_AREA;
    }

    public int getHEIGHT_STREAMING_AREA() {
        return HEIGHT_STREAMING_AREA;
    }

    public StreamingViewProcessor getStreamingViewProcessor() {
        return streamingViewProcessor;
    }
}



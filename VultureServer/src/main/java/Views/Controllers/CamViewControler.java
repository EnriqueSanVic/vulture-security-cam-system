package Views.Controllers;

import Views.CamView;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CamViewControler extends WindowAdapter {

    private CamView view;

    private StreamingViewProcessor streamingViewProcessor;

    public CamViewControler(CamView view, StreamingViewProcessor streamingViewProcessor) {
        this.view = view;
        this.streamingViewProcessor = streamingViewProcessor;
    }

    public void windowClosing(WindowEvent e){
        synchronized (streamingViewProcessor){
            //stop processor thread
            streamingViewProcessor.setActive(false);
            streamingViewProcessor.notify();
        }

        view.dispose();
    }
}

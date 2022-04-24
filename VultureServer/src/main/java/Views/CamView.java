package Views;

import Server.StreamingListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CamView extends JFrame implements StreamingListener {

    private final int WIDTH = 640, HEIGHT = 480;

    private ImageViewProcessor imageProcessor;

    private CamView own;

    public CamView(){

        super();

        this.setSize(WIDTH, HEIGHT);

        this.own = this;

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                synchronized (imageProcessor){
                    imageProcessor.active = false;
                    imageProcessor.notify();
                }
                own.setVisible(false);
            }
        });

        this.setLayout(null);

        this.setVisible(true);

        imageProcessor = new ImageViewProcessor(this);

        imageProcessor.start();

    }


    @Override
    public void nextFrame(byte[] frame) {

        synchronized(imageProcessor){

            imageProcessor.nextFrameBytes = frame;
            imageProcessor.notify();
        }

    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }
}

class ImageViewProcessor extends Thread{

    private CamView view;

    public byte[] nextFrameBytes;

    public boolean active;

    private BufferStrategy doubleBuffer;

    private Graphics doubleBufferGraphics;


    public ImageViewProcessor(CamView view) {

        this.view = view;

        this.active = true;

        this.setPriority(Thread.MAX_PRIORITY);

        RepaintManager rm = RepaintManager.currentManager(this.view);

        rm.setDoubleBufferingEnabled(true);

        this.view.createBufferStrategy(2);

        this.doubleBuffer = this.view.getBufferStrategy();

        doubleBufferGraphics = this.doubleBuffer.getDrawGraphics();
    }

    @Override
    public void run() {

        Image image;

        while(active){

            waitNotify();

            doubleBufferGraphics.fillRect(0, 0, view.getWIDTH(), view.getHEIGHT());

            image = toBufferedImage(nextFrameBytes);

            doubleBufferGraphics.drawImage(image,20,20,null);

            doubleBuffer.show();

            System.out.println("Tick");


        }
    }


    private BufferedImage toBufferedImage(byte[] bytes) {

        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi;

    }

    private synchronized void waitNotify(){
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

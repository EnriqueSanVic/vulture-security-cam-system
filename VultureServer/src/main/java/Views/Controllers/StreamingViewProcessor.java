package Views.Controllers;

import Server.CamThread;
import Server.StreamingListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Hilo procesador del streaming que le es redirigido desde el hilo productor seleccionado.
 * Este hilo tiene la finalidad de escuchar el streaming y pintarlo en el canvas de la vista.
 */
public class StreamingViewProcessor extends Thread implements StreamingListener {

    private final Canvas view;

    private CamThread camThread;

    private byte[] nextFrameBytes;

    private boolean active;

    private final BufferStrategy doubleBuffer;

    private final Graphics2D doubleBufferGraphics;


    public StreamingViewProcessor(Canvas view) {

        this.view = view;

        this.active = true;

        this.setPriority(Thread.MAX_PRIORITY);

        this.view.createBufferStrategy(2);

        this.doubleBuffer = this.view.getBufferStrategy();

        doubleBufferGraphics = (Graphics2D) this.doubleBuffer.getDrawGraphics();
    }

    @Override
    public void run() {

        Image image;

        while (active) {

            waitNotify();

            doubleBufferGraphics.fillRect(0, 0, view.getWidth(), view.getHeight());

            image = toBufferedImage(nextFrameBytes);

            doubleBufferGraphics.drawImage(image, 0, 0, null);

            try {
                doubleBuffer.show();
            } catch (IllegalStateException ex) {

            }
        }

        //cuando termina de procesar imágenes se quita como escuchador del camThread
        camThread.removeStreamingListener(this);

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

    @Override
    public void nextFrame(byte[] frame) {

        synchronized (this) {

            this.setNextFrameBytes(frame);
            this.notify();
        }

    }

    @Override
    public void setCamThread(CamThread camThread) {
        this.camThread = camThread;
    }

    private synchronized void waitNotify() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setNextFrameBytes(byte[] nextFrameBytes) {
        this.nextFrameBytes = nextFrameBytes;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

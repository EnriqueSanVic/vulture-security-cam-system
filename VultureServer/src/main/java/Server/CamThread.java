package Server;

import Stream.VideoManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CamThread extends Thread{

    private final int CLIP_DURATION_MINS = 1;

    private final long CLIP_DURATION_NANO_SECS = CLIP_DURATION_MINS * 60000000000l;

    private final int MAX_CAM_NAME_BYTES = 20;

    private final String BACKFILL_CHARACTER_STRINGS = "*";

    private ArrayList<StreamingListener> streamingListeners;

    private Socket socket;
    private InputStream input;
    private OutputStream output;

    private VideoManager video;

    private int clientId;
    private String camName;

    boolean active, cameraOn;

    long initTime, actualTime, diffTime;

    public CamThread(Socket socket) {
        this.socket = socket;
        this.active = false;
        this.cameraOn = true;

        this.setPriority(Thread.MAX_PRIORITY);

        streamingListeners = new ArrayList<StreamingListener>();

        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addStreamingListener(StreamingListener listener){
        streamingListeners.add(listener);
    }

    public void removeStreamingListener(StreamingListener listener){
        streamingListeners.remove(listener);
    }

    @Override
    public void run() {

        byte[] signalbytes;

        try{

            //de primeras se lee el número de cliente al que está asociado la cámara
            clientId = readSignedInt32();

            System.out.println("Cliente nº: " + clientId);

            //se lee el nombre de la cámara con una longitud fija de 20 bytes
            camName = readString(MAX_CAM_NAME_BYTES);

            System.out.println("Cámara: " + camName);

        }catch (IOException ex){
            ex.printStackTrace();
        }

        //bucle de inicio de streaming constante mientras la camara esté encendidad
        while(cameraOn){

            while(!active){
                threadSleep();
            }

            signalbytes = intToByteArray(VultureCamSignals.START_STREAMING_TO_CAMERA_SIGNAL);


            try {
                output.write(signalbytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            leaseStreaming();
        }

        closeSocketConnection();

    }

    public synchronized void startStreaming(){
        active = true;
        this.notify();
    }

    public void stopStreaming(){

        //se le envía una señal a la cámara para que deje de enviar el streaming
        try {
            output.write(intToByteArray(VultureCamSignals.STOP_STREAMING_TO_CAMERA_SIGNAL));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void shutdownStreaming(){

        //se le envía una señal a la cámara para que apague el sistema
        try {
            output.write(intToByteArray(VultureCamSignals.SHUTDOWN_CAMERA_TO_CAMERA_SIGNAL));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void leaseStreaming(){

        System.out.println("Iniciada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        int bufferSizeSignal = 0;
        byte[] bytes;

        active = true;

        video = new VideoManager();

        try{

            video.startMp4Encode(generateClipPath());

            initTime = System.nanoTime();

            //se comienza la lectura
            while(active && cameraOn){

                //se lee el número de bytes que va a tener el próximo frame que se envíe por el socket
                bufferSizeSignal = readSignedInt32();

                //si la señal es negativa significa que es una señal de comunicación del protocolo
                if(bufferSizeSignal < 0) {

                    processSignal(bufferSizeSignal);

                //si es positiva es el tamaño del buffer en bytes por lo tanto se procede a procesar el siguietne frame
                }else{
                    System.out.println("Tamaño frame: " + bufferSizeSignal + " bytes");

                    //se hace una lectura de los siguientes n bytes para leer la imagen
                    bytes = input.readNBytes(bufferSizeSignal);

                    //se manda el frame a todos los escuchadores del streaming
                    for(StreamingListener listener:streamingListeners){
                        listener.nextFrame(bytes);
                    }

                    video.nextFrame(bytes);

                    actualTime = System.nanoTime();

                    //si el tiempo de grabación del clip es superior al establecido se guarda un clip y se crea otro
                    if(isClipEnd()){
                        video.stopAndSave();
                        video.startMp4Encode(generateClipPath());
                        initTime = System.nanoTime();
                    }
                }
            }

            System.out.println("Parada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        }catch (IOException ex){
            ex.printStackTrace();
        }

        //Si el clip estaba codificando se cierra y se guarda.
        if(video.isEncoding()){
            video.stopAndSave();
        }

    }


    private void processSignal(int signal){

        switch(signal){

            case VultureCamSignals.CONFIRM_STOP_STREAMING_FROM_CAMERA_SIGNAL:
                active = false;
                break;

            case VultureCamSignals.CONFIRM_SHUTDOWN_CAMERA_FROM_CAMERA_SIGNAL:
                active = false;
                cameraOn = false;
                break;
        }

    }


    private void closeSocketConnection() {

        try {
            input.close();
            socket.close();

            System.out.println("Apagada la cámara " + camName + " del cliente id " + clientId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void threadSleep(){
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    private boolean isClipEnd(){

        diffTime = actualTime - initTime;

        System.out.println("Segs: " + (diffTime / 1000000000l));

        return (diffTime >= CLIP_DURATION_NANO_SECS);
    }

    private int numeroClip = 0;

    private String generateClipPath(){
        ++numeroClip;
        return "./video" + numeroClip + ".mp4";
    }

    private int readSignedInt32 () throws IOException {

        //se leen los siguientes 4 primeros bytes, que son los 323 bits de un numero entero
        byte[] bytes = input.readNBytes(Integer.BYTES);

        //se transforman los 4 bytes del signed integer, el tipo primitivo int.
        return ByteBuffer.wrap(bytes).getInt();
    }

    private String readString(int nBytes) throws IOException {

        String cadena;

        byte[] strBytes = input.readNBytes(nBytes);

        cadena = new String(strBytes, StandardCharsets.UTF_8);

        //Se eliminan los caracteres de relleno
        return cadena.replace(BACKFILL_CHARACTER_STRINGS,"");

    }

    private byte[] intToByteArray(int num){
        return ByteBuffer.allocate(4).putInt(num).array();
    }
}

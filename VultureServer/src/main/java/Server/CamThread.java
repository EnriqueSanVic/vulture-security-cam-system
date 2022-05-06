package Server;

import Database.DBController;
import Exceptions.AuthException;
import FileSaveSystem.ClipHandler;
import Models.Camera;
import Models.User;
import VideoUtils.VideoManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CamThread extends Thread{

    private final int CLIP_DURATION_MINS = 10;

    private final long CLIP_DURATION_NANO_SECS = CLIP_DURATION_MINS * 60000000000l;

    private final int MAX_CAM_NAME_BYTES = 20;

    private final String BACKFILL_CHARACTER_STRINGS = "*";

    private ArrayList<StreamingListener> streamingListeners;

    private Socket socket;
    private InputStream input;
    private OutputStream output;

    private DBController database;
    private VideoManager video;

    private int clientId;
    private String camName;
    private User user;
    int camID;
    private Camera camera;

    boolean active, cameraOn;

    long initTime, actualTime, diffTime;

    public CamThread(Socket socket) {
        this.socket = socket;
        this.active = false;
        this.cameraOn = true;

        this.setPriority(Thread.MAX_PRIORITY);

        streamingListeners = new ArrayList<StreamingListener>();

        database = new DBController();

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
        boolean isAuth = true;

        database.openConnection();

        try {
            reciveInitialSignals();
        } catch (AuthException e) {
            isAuth = false;
            System.out.println(e.getMessage());
        }

        //bucle de inicio de streaming constante mientras la camara esté encendidad
        while(cameraOn && isAuth){

            while(!active){
                threadSleep();
            }

            if(database.isClosed()){
                database.openConnection();
            }

            signalbytes = intToByteArray(VultureCamSignals.START_STREAMING_TO_CAMERA_SIGNAL);

            try {
                output.write(signalbytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                leaseStreaming();
            } catch (IOException e) {
                //se evita el problema del bucle de creacion de ficheros
                cameraOn = false;
                e.printStackTrace();
            }

            //always closed connection when cam is paused
            database.closeConnection();
        }

        //if the user not authenticated then close database
        if(!isAuth){
            shutdownStreaming();
            if(database != null) database.closeConnection();
        }

        closeSocketConnection();

    }

    private void reciveInitialSignals() throws AuthException {

        boolean correctAuth = true;

        try{

            //de primeras se lee el número de cliente al que está asociado la cámara
            clientId = readSignedInt32();

            System.out.println("Cliente nº: " + clientId);

            camID = readSignedInt32();

            System.out.println("Cámara id: " + camID);

            //se lee el nombre de la cámara con una longitud fija de 20 bytes
            camName = readString(MAX_CAM_NAME_BYTES);

            System.out.println("Cámara: " + camName);

            //find and get user
            user = database.findUser(clientId);

            if(user == null){
                correctAuth = false;
            }

            //find and get camera
            if(user != null){
                camera = database.findCamera(user, camID);

                //if the camera don´t exist in the db then create it
                if(camera == null){
                    database.createCamera(user, camID, camName);
                }
            }

        }catch (IOException ex){
            correctAuth = false;
        }

        if(!correctAuth){
            throw new AuthException(clientId);
        }

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


    private void leaseStreaming() throws IOException {

        System.out.println("Iniciada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        int bufferSizeSignal = 0;
        byte[] bytes;

        active = true;

        video = new VideoManager();

            video.startMp4Encode(ClipHandler.generateClipTempPath(user, camera));

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
                        video.startMp4Encode(ClipHandler.generateClipTempPath(user, camera));
                        initTime = System.nanoTime();
                    }
                }
            }

            System.out.println("Parada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);


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

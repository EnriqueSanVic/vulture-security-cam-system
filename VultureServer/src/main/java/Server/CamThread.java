package Server;

import Database.DBController;
import Exceptions.AuthException;
import FileSaveSystem.ClipHandler;
import Models.Camera;
import Models.Record;
import Models.User;
import VideoUtils.VideoManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class CamThread extends Thread{

    private final int CLIP_DURATION_MINS = 15;

    private final long CLIP_DURATION_NANO_SECS = CLIP_DURATION_MINS * 60000000000l;

    private final int MAX_CAM_NAME_BYTES = 20;

    private final String BACKFILL_CHARACTER_STRINGS = "*";

    private final ArrayList<StreamingListener> streamingListeners;

    private final Socket socket;
    private InputStream input;
    private OutputStream output;

    private final DBController database;
    private VideoManager video;

    private int clientId;
    private String camName;
    private User user;
    private int camID;
    private Camera camera;

    private Record record;
    private byte[] bytes;


    private boolean active, cameraOn;

    private LocalDateTime initDate,finalDate;
    private long initTime, actualTime, diffTime;

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
        System.out.println("Streaming listener añadido correctamente");
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
            refreshDataBaseState();
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

        System.out.println("Cloe camera thread:" + this.getId());

    }

    private void refreshDataBaseState() {

        LocalDateTime dateNow = LocalDateTime.now();

        //refresh camera log fields
        database.updateCamera(camera, camName, dateNow, this.getId());

    }

    private void reciveInitialSignals() throws AuthException {

        try{

            //de primeras se lee el número de cliente al que está asociado la cámara
            clientId = readSignedInt32();

            System.out.println("Cliente nº: " + clientId);

            camID = readSignedInt32();

            System.out.println("Cámara id: " + camID);

            //se lee el nombre de la cámara con una longitud fija de 20 bytes
            camName = readString(MAX_CAM_NAME_BYTES);

            System.out.println("Cámara: " + camName);

             authCredentials();

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

    private void authCredentials() throws AuthException {

        //find and get user
        user = database.findUser(clientId);

        //find and get user and camera
        if(user != null){

            camera = database.findCamera(user, camID);

            if(camera == null){
                throw new AuthException(-1, camID, camName);
            }

           /*
             Compare the user id and the camera user id is the same.
             if this expression return true means that the camera belongs to user
             but its different throws an exception
           */
           if(user.getId() != camera.getId_user()){
               throw new AuthException(user.getId(), camera.getId(), camera.getName());
           }
        }else{
            throw new AuthException(clientId, -1, "");
        }

    }

    public synchronized void startStreaming(){
        active = true;
        this.notify();
    }

    public void stopStreaming(){

        //se le envía una señal a la cámara para que deje de enviar el streaming
        try {
            if(!socket.isClosed())
            output.write(intToByteArray(VultureCamSignals.STOP_STREAMING_TO_CAMERA_SIGNAL));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void shutdownStreaming(){

        //se le envía una señal a la cámara para que apague el sistema
        try {
            output.write(intToByteArray(VultureCamSignals.SHUTDOWN_CAMERA_TO_CAMERA_SIGNAL));
            initCloseTimeOut();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initCloseTimeOut() {

        Thread timeOutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //slepp the timeoutthread
                    Thread.sleep(10000);
                    interruptThread();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        timeOutThread.start();

    }

    /**
     * Este método fuerza el cierre del hilo interrumpiendo las conexiones.
     */
    private void interruptThread(){
        if(active || cameraOn){
            active = false;
            cameraOn = false;
            closeSocketConnection();
        }
    }


    private void leaseStreaming() throws IOException {

        System.out.println("Iniciada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        int bufferSizeSignal = 0;

        active = true;

        video = new VideoManager();

            video.startMp4Encode(ClipHandler.generateClipTempPath(user, camera));

            initDate = LocalDateTime.now();
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
                    processFrame(bufferSizeSignal);
                    checkEndClip();
                }
            }

            System.out.println("Parada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        //Si el clip estaba codificando se cierra y se guarda.
        if(video.isEncoding()){
            stopAndSaveFinalClip();
        }

        active = false;

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

    private void processFrame(int bufferSizeSignal) throws IOException {

        //se hace una lectura de los siguientes n bytes para leer la imagen
        bytes = input.readNBytes(bufferSizeSignal);

        //se manda el frame a todos los escuchadores del streaming
        for(StreamingListener listener:streamingListeners){
            listener.nextFrame(bytes);
        }

        video.nextFrame(bytes);

    }

    private void checkEndClip() throws IOException {

        actualTime = System.nanoTime();

        //si el tiempo de grabación del clip es superior al establecido se guarda un clip y se crea otro
        if(isClipEnd()){

            stopAndSaveFinalClip();

            video.startMp4Encode(ClipHandler.generateClipTempPath(user, camera));
            //reinit the time

            initDate = LocalDateTime.now();
            initTime = System.nanoTime();
        }
    }

    private void stopAndSaveFinalClip() {
        //generate final date
        finalDate = LocalDateTime.now();

        //save and close video file
        video.stopAndSave();

        //create temporal objet model the path null
        record = new Record(initDate, finalDate, null, camera.getId());

        //first change the temporal path for final path, this method return the complet record with final path
        record = ClipHandler.moveTemFileToFinalPath(user, camera, record);

        //then save the model in the data base
        database.saveRecord(record);
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
        }
    }


    private boolean isClipEnd(){

        diffTime = actualTime - initTime;
        //System.out.println("Segs: " + (diffTime / 1000000000l));

        return (diffTime >= CLIP_DURATION_NANO_SECS);
    }




    private int readSignedInt32 () throws IOException {

        //se leen los siguientes 4 primeros bytes, que son los 32 bits de un numero entero
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

    public void setCameraOn(boolean cameraOn) {
        this.cameraOn = cameraOn;
    }

    public boolean isActive() {
        return active;
    }
}

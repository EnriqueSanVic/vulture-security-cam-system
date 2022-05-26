package Server;

import Database.DBController;
import Models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;

public class ClientApiThread extends Thread implements StreamingListener{

    private static final ClientResponse STATUS_TRUE_RESPONSE = new ClientResponse(true, 0, null, null, 0);
    private static final ClientResponse STATUS_FALSE_RESPONSE = new ClientResponse(false, 0, null, null, 0);

    private final ServerCamConnectionsHandler serverCams;
    private final Socket socket;

    private DataInputStream inputHighLevel;
    private InputStream inputLowLevel;
    private DataOutputStream outputHighLevel;
    private OutputStream outputLowLevel;

    private boolean active, isAuth = false, activeStreaming, waitFornextFrame;

    private User user;

    private CamThread camThread;

    private Gson gson;

    private final DBController database;

    private byte[] nextFrameBytes;

    public ClientApiThread(Socket socket, ServerCamConnectionsHandler serverCams) {

        this.socket = socket;
        this.serverCams = serverCams;
        this.active = true;

        this.gson = new GsonBuilder().create();

        this.setPriority(Thread.MAX_PRIORITY);

        database = new DBController();

        try {
            inputLowLevel = socket.getInputStream();
            inputHighLevel = new DataInputStream(inputLowLevel);
            outputLowLevel = socket.getOutputStream();
            outputHighLevel = new DataOutputStream(outputLowLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        String requestJson;
        ClientRequest request;
        Gson gson = new GsonBuilder().create();


        database.openConnection();

        try{

            while (active){

                requestJson = inputHighLevel.readUTF();

                System.out.println("Request: " + requestJson);

                request = gson.fromJson(requestJson, ClientRequest.class);

                processRequest(request);
            }

        }catch (IOException ex){
            ex.printStackTrace();
        }

        database.closeConnection();

        closeSocketConnection();

    }

    private void processRequest(ClientRequest request) {

        switch (request.getRequest()){

            case "login":
                authUser(request);
                break;

            case "list_of_cams":
                if(isAuth){
                    sendListOfCams();
                }
                break;

            case "list_of_clips":
                if(isAuth){
                    sendListOfClips(request);
                }
                break;

            case "clip_download":
                if(isAuth){
                    downloadClip(request);
                }
                break;

            case "streaming_transmision":
                if(isAuth){
                    streamingTransmision(request);
                }
                break;

            default:
                break;
        }

    }

    private void streamingTransmision(ClientRequest request) {

        Camera camera = database.findCamera(request.getCamId());
        long camThreadId = camera.getRef_hilo();

        if(serverCams.existCamThread(camThreadId)){
            writeResponseInSocket(STATUS_TRUE_RESPONSE);
            initStreaming(camThreadId);
        }else{
            System.out.println("NO ha logrado encontrar la camara");
            writeResponseInSocket(STATUS_FALSE_RESPONSE);
        }

    }

    private void initStreaming(long camThreadId) {

        byte[] frameLengthBytes;

        serverCams.setStreamingListenerToCamThread(this, camThreadId);

        activeStreaming = true;
        waitFornextFrame = true;

        try{

            while (activeStreaming){

                System.out.println("Tansmitiendo a un cliente");

                waitFornextFrame = true;

                while (waitFornextFrame){
                    threadSleep();
                }

                frameLengthBytes = intToBytes(nextFrameBytes.length);

                for(byte i:frameLengthBytes){
                    System.out.print(" - " + i);
                }

                System.out.print("\n");

                System.out.println("Bytes size: " + nextFrameBytes.length);

                outputLowLevel.write(frameLengthBytes);

                outputLowLevel.write(nextFrameBytes);

            }

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

    private void downloadClip(ClientRequest request) {

        ClientResponse response;
        byte[] clipBytes = {};

        Record record = database.findRecord(request.getClipId());

        File clip = new File(record.getPath());

        try {
            clipBytes = Files.readAllBytes(clip.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(clip.exists() && clipBytes.length > 0){

            response = new ClientResponse(true, 0, null, null,  clipBytes.length);
            writeResponseInSocket(response);

            try {
                outputLowLevel.write(clipBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{

            response = new ClientResponse(false, 0, null, null, 0);
            writeResponseInSocket(response);

        }

    }


    private void sendListOfClips(ClientRequest request) {

        Camera selectedCamera = database.findCamera(request.getCamId());

        ArrayList<Record> listRecord = database.getCamClips(selectedCamera);

        ClientResponse response = new ClientResponse(true, 0, ClientResponse.ClipResponse.mutateList(listRecord), null, 0);

        writeResponseInSocket(response);
    }

    private void sendListOfCams() {

        ArrayList<Camera> listCams = database.getUserCameras(user);

        ClientResponse response = new ClientResponse(true, 0, null, ClientResponse.CamResponse.mutateList(listCams), 0);

        writeResponseInSocket(response);
    }

    private void authUser(ClientRequest request) {

        user = database.findUser(request.getUser(), request.getPassword());

        isAuth = (user != null);
        //VALIDATE USER IN DATABASE
        ClientResponse response = isAuth ? STATUS_TRUE_RESPONSE : STATUS_FALSE_RESPONSE;

        writeResponseInSocket(response);

    }

    private void writeResponseInSocket(ClientResponse response){
        try {
            outputHighLevel.writeUTF(gson.toJson(response, ClientResponse.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocketConnection() {

        try {
            inputLowLevel.close();
            socket.close();

            System.out.println("Cerrada conexion cliente");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void nextFrame(byte[] frame) {

        this.setNextFrameBytes(frame);
        waitFornextFrame = false;
        this.notify();
    }

    @Override
    public void setCamThread(CamThread camThread) {
        this.camThread = camThread;
    }

    public synchronized void setNextFrameBytes(byte[] nextFrameBytes) {
        this.nextFrameBytes = nextFrameBytes;
    }

    private synchronized void threadSleep(){
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] intToBytes(int num){
        return ByteBuffer.allocate(Integer.BYTES).putInt(num).array();
    }
}

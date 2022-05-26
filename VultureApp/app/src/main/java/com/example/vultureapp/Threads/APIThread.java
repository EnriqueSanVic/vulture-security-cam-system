package com.example.vultureapp.Threads;

import android.content.Context;

import com.example.vultureapp.Callbacks.FrameCallBack;
import com.example.vultureapp.Callbacks.ThreadCallBack;
import com.example.vultureapp.Models.Request;
import com.example.vultureapp.Models.Response;
import com.example.vultureapp.Views.ClipView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class APIThread extends Thread{

    public static final int CONFIRM_STOP_STREAMING_FROM_CAMERA_SIGNAL = -2341;
    public static final int CONFIRM_SHUTDOWN_CAMERA_FROM_CAMERA_SIGNAL = -6732;

    public static final int START_STREAMING_TO_CAMERA_SIGNAL = 54;
    public static final int STOP_STREAMING_TO_CAMERA_SIGNAL = 34;
    public static final int SHUTDOWN_CAMERA_TO_CAMERA_SIGNAL = 78;

    private final String IP = "192.168.1.68";
    private final int PORT = 4547;

    private ClipView fileHandler;
    private Socket socket;

    private boolean active, isWait, isAuth, nextReadFile, nextInitStreaming;

    private long nextFileNBytes;

    private byte[] frameBuffer = new byte[700000];
    private int bytesLen, fileBytesLen;

    private String json = "";
    private String dataResponse;

    private ThreadCallBack nextCallback = null;
    private FrameCallBack nextFrameCallBack = null;
    private Request nextRequest = null;

    private DataInputStream inputHighLevel;
    private InputStream inputLowLevel;
    private DataOutputStream outputHighLevel;
    private OutputStream outputLowLevel;

    private Gson gson;

    private String user, password;

    private static APIThread singleton;

    static{
        singleton = new APIThread();
    }

    public static APIThread getInstance(){
        return singleton;
    }

    private APIThread() {
        this.gson = new GsonBuilder().create();
    }

    public void setCredentials(String user, String password){
        this.user = user;
        this.password = password;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public void setNextCallback(ThreadCallBack callBack){
        this.nextCallback = callBack;
    }

    public void setNextFrameCallBack(FrameCallBack callBack){
        this.nextFrameCallBack = callBack;
    }

    public void setNextRequest(Request nextRequest) {
        this.nextRequest = nextRequest;
    }

    public void sendRequest(){

        synchronized (this){
            isWait = false;
            notify();
        }

    }

    public void closeConnection(){

        System.out.println("Close conection order");
        synchronized (this){
            active = false;
            isWait = false;
            notify();
        }

    }

    @Override
    public void run() {

        System.out.println("start api service");

        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(IP, PORT), 3000);

            inputLowLevel = socket.getInputStream();
            inputHighLevel = new DataInputStream(inputLowLevel);


            outputLowLevel = socket.getOutputStream();
            outputHighLevel = new DataOutputStream(outputLowLevel);

            active = true;
            isWait = true;
            isAuth = false;
            nextReadFile = false;
            nextInitStreaming = false;

            while(active){

                while(isWait){
                    threadWait();
                }

                isWait = true;

                if(active){

                    if(nextReadFile){
                        nextReadFile = false;
                        readFile();
                    }else if(nextInitStreaming){
                        nextInitStreaming = false;
                        initStreaming();
                    }else{
                        handleHighLevelRequest();
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if(!socket.isClosed()) closeSocket();

        System.out.println("close api service");

    }

    private void initStreaming() throws IOException {

        bytesLen = readSignedInt32();

        while(true){

            readNBytesInFrameBuffer();

            nextFrameCallBack.callBack(frameBuffer, bytesLen);

            bytesLen = readSignedInt32();
        }

    }

    private void readFile() throws IOException {

        fileBytesLen = (int)nextFileNBytes;

        byte[] fileBytes = readNBytes(fileBytesLen);

        fileHandler.handleFile(fileBytes);

    }


    //Read n Bytes from socket
    private void readNBytesInFrameBuffer() throws IOException{

        System.out.println("Bytes Size: " + bytesLen);

        int actualNBytes = 0, successfulBytes;

        while(actualNBytes < bytesLen){

            successfulBytes = inputLowLevel.read(frameBuffer, actualNBytes, bytesLen - actualNBytes);

            if(successfulBytes == -1){
                System.out.println("ROTOOOOOOOOOOOOOO");
                break;
            }

            actualNBytes += successfulBytes;

            //System.out.println("Bytes Leidos (%) " + ((double)actualNBytes/(double)bytesLen)*100 + "%");
        }

        System.out.println("Total bytes =  " + actualNBytes + "\n Total bytes = " + bytesLen);

    }

    private byte[] readNBytes(int nBytes) throws IOException{

        System.out.println("Bytes Size: " + nBytes);

        byte[] buffer = new byte[nBytes];

        int actualNBytes = 0, successfulBytes;

        while(actualNBytes < nBytes){

            successfulBytes = inputLowLevel.read(buffer, actualNBytes, nBytes - actualNBytes);

            if(successfulBytes == -1){
                System.out.println("ROTOOOOOOOOOOOOOO");
                break;
            }

            actualNBytes += successfulBytes;

            //System.out.println("Bytes Leidos (%) " + ((double)actualNBytes/(double)nBytes)*100 + "%");
        }

        return buffer;

    }

    private int readSignedInt32 () throws IOException {

        //se leen los siguientes 4 primeros bytes, que son los 32 bits de un numero entero
        byte[] bytes = readNBytes(Integer.BYTES);

        for(byte i:bytes){
            System.out.print(" - " + i);
        }

        System.out.print("\n");

        //se transforman los 4 bytes del signed integer, el tipo primitivo int.
        return ByteBuffer.wrap(bytes).getInt();
    }

    private void handleHighLevelRequest() throws IOException {

        if(!isAuth){
            json = gson.toJson(new Request(Request.LOGIN_REQUEST_COMMAND, user, password, 0, 0));
        }else{
            if(nextRequest != null){
                json = gson.toJson(nextRequest);
            }
        }

        System.out.println(json);

        outputHighLevel.writeUTF(json);

        dataResponse = inputHighLevel.readUTF();

        System.out.println(dataResponse);

        if(nextCallback != null){
            nextCallback.callBack(gson.fromJson(dataResponse, Response.class));
            nextCallback = null; //destroy the callback when use it
        }

    }

    private void closeSocket(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void threadWait() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void readFile(long nBytes){
            nextReadFile = true;
            nextFileNBytes = nBytes;
            isWait = false;
    }

    public void setFileHandler(ClipView fileHandler) {
        this.fileHandler = fileHandler;
    }

    public void setNextInitStreaming(boolean init){
        this.nextInitStreaming = init;
    }
}

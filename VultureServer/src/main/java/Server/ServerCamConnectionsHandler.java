package Server;

import FileSaveSystem.ClipHandler;
import Views.CamView;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerConnectionsManager extends Thread{

    private final String IP = "192.168.1.83";
    private final int PORT = 4548;

    private ServerSocket server;

    private boolean active;

    private HashMap<Long, CamThread> camList;

    public ServerConnectionsManager(){

        this.setPriority(Thread.MAX_PRIORITY);

        active = false;

        camList = new HashMap<Long, CamThread>();

        //make directorie tree if not exist
        ClipHandler.evaluateBasePath();

    }

    @Override
    public void run() {

        active = true;

        try{

            server = new ServerSocket();

            server.bind(new InetSocketAddress(IP, PORT));

            System.out.println("Servicio iniciado en " + IP + " escuchando puerto " + PORT);

            Socket socket;

            CamThread camThread;

            while (active){

                System.out.println("Esperando conexion ...");

                socket = server.accept();

                System.out.println("Conexion aceptada");

                camThread = new CamThread(socket);

                camList.put(Long.valueOf(camThread.getId()),camThread);

                camThread.start();

                System.out.println("Camara iniciada");

                camThread.startStreaming();


            }


            //close server connection
            if(server != null){
                server.close();
            }

        }catch (IOException ex){
            System.out.println("server socket is closed");
        }

        active = false;

    }


    public void shutdownAllStreamings(){

        Iterator<Map.Entry<Long, CamThread>> iter = camList.entrySet().iterator();

        //recorre todas las cámaras mandando la señal de apagar el streaming
        while (iter.hasNext()){
            iter.next().getValue().shutdownStreaming();
        }

        //cuando lo haya hecho interrumpirá el socket para que no se quede bloqueado en el hilo en el método .accept() para que no acepte nuevas conexiones
        try {
            if(server != null){
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean stopStreaming(long camThreadId){

        CamThread camThread = camList.get(camThreadId);

        if(camThread != null){

            camThread.stopStreaming();

            return true;
        }

        return false;
    }

    public boolean startStreaming(long camThreadId){

        CamThread camThread = camList.get(camThreadId);

        if(camThread != null){

            camThread.startStreaming();

            return true;
        }

        return false;
    }

    public boolean isCamActive(long camThreadId){

        CamThread camThread = camList.get(camThreadId);

        if(camThread != null){

            return camThread.isActive();
        }

        return false;
    }

    public boolean setStreamingListenerToCamThread(CamView view, long camThreadId){

        CamThread camThread = camList.get(camThreadId);

        if(camThread != null){

            //se le da el camThread al procesador de imagen de la vista
            view.getStreamingViewProcessor().setCamThread(camThread);

            //add the streaming processor
            camThread.addStreamingListener(view.getStreamingViewProcessor());

            return true;
        }

        return false;
    }

    public boolean removeStreamingListenerToCamThread(CamView view, long camThreadId){

        CamThread camThread = camList.get(camThreadId);

        if(camThread != null){

            //se le da el camThread al procesador de imagen de la vista
            view.getStreamingViewProcessor().setCamThread(null);

            //add the streaming processor
            camThread.removeStreamingListener(view.getStreamingViewProcessor());

            return true;
        }

        return false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


}
package Server;

import FileSaveSystem.ClipHandler;
import Views.CamView;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerConnectionsManager {

    private final String IP = "192.168.1.68";
    private final int PORT = 4548;

    private ServerSocket server;

    private ArrayList<CamThread> camList;

    public ServerConnectionsManager(){

        camList = new ArrayList<CamThread>();

        ClipHandler.evaluateBasePath();

        try{

            server = new ServerSocket();

            server.bind(new InetSocketAddress(IP, PORT));

            System.out.println("Servicio iniciado en " + IP + " escuchando puerto " + PORT);

            Socket socket;

            CamThread camThread;

            while (true){

                System.out.println("Esperando conexion ...");

                socket = server.accept();

                System.out.println("Conexion aceptada");

                camThread = new CamThread(socket);

                camList.add(camThread);

                camThread.start();

                //se le da el camThread al procesador de imagen de la vista
                CamView.getInstance().getStreamingViewProcessor().setCamThread(camThread);

                //add the streaming processor
                camThread.addStreamingListener(CamView.getInstance().getStreamingViewProcessor());

                System.out.println("Camara iniciada");

                camThread.startStreaming();

            }

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }




    }
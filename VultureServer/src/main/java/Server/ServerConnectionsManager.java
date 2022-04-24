package Server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerConnectionsManager {

    private final String IP = "192.168.1.108";
    private final int PORT = 4548;

    private ServerSocket server;

    private ArrayList<CamThread> camList;

    public ServerConnectionsManager(){

        camList = new ArrayList<CamThread>();

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

                System.out.println("Camara iniciada");

                Thread.sleep(5000);

                camThread.startLeaseStreaming();

                Thread.sleep(70000);

                camThread.stopLeaseStreaming();

            }

        }catch (IOException ex){
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }




    }
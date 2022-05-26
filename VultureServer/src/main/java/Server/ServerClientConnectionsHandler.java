package Server;

import FileSaveSystem.ClipHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerClientConnectionsHandler extends Thread{

    private final String IP = "192.168.1.68";
    private final int PORT = 4547;

    private ServerCamConnectionsHandler serverCams;

    private ServerSocket server;

    private boolean active;

    private HashMap<Long, ClientApiThread> clientList;

    public ServerClientConnectionsHandler(ServerCamConnectionsHandler serCam){

        this.setPriority(Thread.MAX_PRIORITY);

        this.serverCams = serCam;

        active = false;

        clientList = new HashMap<Long, ClientApiThread>();

    }

    @Override
    public void run() {

        active = true;

        try{

            server = new ServerSocket();

            server.bind(new InetSocketAddress(IP, PORT));

            System.out.println("Servicio iniciado en " + IP + " escuchando puerto " + PORT);

            Socket socket;

            ClientApiThread clientThread;

            while (active){

                System.out.println("Esperando conexion ...");

                socket = server.accept();

                System.out.println("Conexion aceptada");

                clientThread = new ClientApiThread(socket, serverCams);

                clientList.put(Long.valueOf(clientThread.getId()),clientThread);

                clientThread.start();

                System.out.println("hilo cliente iniciado iniciada");

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

}

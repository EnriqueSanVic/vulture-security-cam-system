package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Hilo que gestiona las conexiones de los dispositivos cliente.
 */
public class ServerClientConnectionsHandler extends Thread {

    private final String IP = "192.168.221.147";
    private final int PORT = 4547;

    private final ServerCamConnectionsHandler serverCams;

    private ServerSocket server;

    private boolean active;

    //almacena los hilos que gestionan cada conexión clinete en una tabla de hash para su rápido acceso.
    private final HashMap<Long, ClientApiThread> clientList;

    public ServerClientConnectionsHandler(ServerCamConnectionsHandler serCam) {

        this.setPriority(Thread.MAX_PRIORITY);

        this.serverCams = serCam;

        active = false;

        clientList = new HashMap<Long, ClientApiThread>();

    }

    /**
     * Aceptará conexiones nuevas mientras el servidor está activo
     */
    @Override
    public void run() {

        active = true;

        try {

            server = new ServerSocket();

            server.bind(new InetSocketAddress(IP, PORT));

            System.out.println("Servicio iniciado en " + IP + " escuchando puerto " + PORT);

            Socket socket;

            ClientApiThread clientThread;

            while (active) {

                System.out.println("Esperando conexion ...");

                socket = server.accept();

                System.out.println("Conexion aceptada");

                clientThread = new ClientApiThread(socket, serverCams);

                clientList.put(Long.valueOf(clientThread.getId()), clientThread);

                clientThread.start();

                System.out.println("hilo cliente iniciado iniciada");

            }


            //close server connection
            if (server != null) {
                server.close();
            }

        } catch (IOException ex) {
            System.out.println("server socket is closed");
        }

        active = false;

    }


    //elimina todos los hilos de clinete
    public void shutDownAllClients() {

        active = false;

        //close all child sockets

        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

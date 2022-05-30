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

/**
 * Hilo encargado de gestionar las conexiones de  un cliente.
 * <p>
 * Esa clase implementa la interfaz Streaming listener lo cual lo comvierte en un objeto consumidor
 * del straming generado por un hilo CamThread.
 */
public class ClientApiThread extends Thread implements StreamingListener {

    //respuestas recurrentes que se instancian al iniciar el programa para optimizar.
    private static final ClientResponse STATUS_TRUE_RESPONSE = new ClientResponse(true, 0, null, null, 0);
    private static final ClientResponse STATUS_FALSE_RESPONSE = new ClientResponse(false, 0, null, null, 0);

    //constantes del comando de acción de las peticiones del cliente.
    private final String REQUEST_LOGIN_COMMAND = "login";
    private final String REQUEST_LIST_OF_CAMS_COMMAND = "list_of_cams";
    private final String REQUEST_LIST_OF_CLIPS_COMMAND = "list_of_clips";
    private final String REQUEST_CLIP_DOWNLOAD_COMMAND = "clip_download";
    private final String REQUEST_STREAMING_TRANSMISSION_COMMAND = "streaming_transmision";


    private final ServerCamConnectionsHandler serverCams;
    private final Socket socket;
    private final DBController database;

    /**
     * Este hilo realiza lecturas y escrituras de alto y bajo nivel en el socket.
     * Para las lecturas/escrituras de bajo nivel se crean los objetos inputLowLevel, outputLowLevel respectivamente.
     * Para las lecturas/escrituras de alto nivel se crean los objetos inputHighLevel, outputHighLevel respectivamente.
     */
    private DataInputStream inputHighLevel;
    private InputStream inputLowLevel;
    private DataOutputStream outputHighLevel;
    private OutputStream outputLowLevel;

    private final boolean active;
    private boolean isAuth = false;
    private boolean activeStreaming;
    private boolean waitForNextFrame;
    private boolean nextLenSignalIsShutdown;
    private User user;
    private final ClientApiThread ownThread;
    private CamThread camThread;
    private Thread inputThread;
    private final Gson gson;
    private byte[] nextFrameBytes;
    private boolean activeInputThread;

    public ClientApiThread(Socket socket, ServerCamConnectionsHandler serverCams) {

        this.socket = socket;
        this.serverCams = serverCams;
        this.active = true;

        ownThread = this;

        //se crea el gson para serializar y deserializar objetos en json
        this.gson = new GsonBuilder().create();

        this.setPriority(Thread.MAX_PRIORITY);

        //se instancia el controlador de la base de datos
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

        try {

            //mientras el hilo esté activo
            while (active) {

                //se lee el socket hasta encontrar \n
                requestJson = inputHighLevel.readUTF();

                System.out.println("Request: " + requestJson);

                //se deserializa a una objeto ClientRequest
                request = gson.fromJson(requestJson, ClientRequest.class);

                //se procesa la petición
                processRequest(request);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //cuando acabe la conexión con el cliente se cierra la conexión con la base de datos
        database.closeConnection();

        //se cierra la conexión con el socket
        closeSocketConnection();

    }

    /**
     * Este mñetodo procesa una petición del cliente
     * @param request
     */
    private void processRequest(ClientRequest request) {

        System.out.println("Proceando opetición");

        switch (request.getRequest()) {

            //petición de login
            case REQUEST_LOGIN_COMMAND:
                authUser(request);
                break;

            //petición de listado de cámaras
            case REQUEST_LIST_OF_CAMS_COMMAND:
                if (isAuth) {
                    sendListOfCams();
                }
                break;

            //petición de listado de clips
            case REQUEST_LIST_OF_CLIPS_COMMAND:
                if (isAuth) {
                    sendListOfClips(request);
                }
                break;

            //petición de descarga de un clip
            case REQUEST_CLIP_DOWNLOAD_COMMAND:
                if (isAuth) {
                    downloadClip(request);
                }
                break;

            //petición de inicio de streaming
            case REQUEST_STREAMING_TRANSMISSION_COMMAND:
                if (isAuth) {
                    streamingTransmision(request);
                }
                break;

            default:
                System.out.println("Petición no identificada.");
                break;
        }

    }

    /**
     * Método que inicia la redirección del streaming al cliente.
     * @param request
     */
    private void streamingTransmision(ClientRequest request) {

        //se recoge el objeto cámara por el id que ha solicitado el cliente en al petición.
        Camera camera = database.findCamera(request.getCamId());
        long camThreadId = camera.getRef_hilo();

        //si la cámara existe
        if (serverCams.existCamThread(camThreadId)) {
            //se envía una respuesta positiva a la petición indicando que va a comenzar el bucle de envío de frames del protocolo de streaming
            writeResponseInSocket(STATUS_TRUE_RESPONSE);
            //se inicia la redirección del streaming
            initStreaming(camThreadId);
        } else {
            System.out.println("No se ha logrado encontrar la camara");
            //se envía una respuesta negativa para indicar que no se ha encontrado la cámara o que esta no está transmitiendo
            writeResponseInSocket(STATUS_FALSE_RESPONSE);
        }

    }

    private void initStreaming(long camThreadId) {

        byte[] frameLengthBytes;

        //se manda este hilo consumidor al hilo productor del streaming por medio de la interfaz de escuchador de streaming.
        serverCams.setStreamingListenerToCamThread(this, camThreadId);

        //se inicia el hilo de escucha de las señales de cliente
        initInputThread();

        //se inicia el estado de las flags
        activeStreaming = true;
        waitForNextFrame = true;
        nextLenSignalIsShutdown = false;

        try {
            //mientras el streaming esté activo
            while (activeStreaming) {

                System.out.println("Tansmitiendo a un cliente");

                //se espera a la siguiente notificación del hilo productor de streaming
                waitForNextFrame = true;

                //se bloquea el hilo mientras no se le notifique
                while (waitForNextFrame) {
                    threadSleep();
                }

                //si la siguiente señal es la seál de apagado
                if (nextLenSignalIsShutdown) {

                    //se para el bucle
                    activeStreaming = false;
                    System.out.println("SHUTDOWN SIGNAL");

                } else {

                    //se cogen los bytes del número entero de 32 bits con signo que representa el tamaño del siguiente frame que se redirigirá al cliente.
                    frameLengthBytes = intToBytes(nextFrameBytes.length);

                    System.out.println("Bytes size: " + nextFrameBytes.length);

                    //se manda por el canal de bajo nivel los bytes del número entero que representan el tamaño del frame
                    outputLowLevel.write(frameLengthBytes);

                    //se mandan los bytes del frame
                    outputLowLevel.write(nextFrameBytes);

                }

            }

            //se quita este hilo consimidor de steaming como escuchador del hilo productor
            serverCams.removeStreamingListenerToCamThread(this, camThreadId);

            //se le manda al cliente la señal de apagado del streaming para que actúen en consecuencia
            outputLowLevel.write(intToBytes(VultureCamSignals.CONFIRM_SHUTDOWN_CAMERA_FROM_CAMERA_SIGNAL));


        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }

    /**
     * Este metodo levanta el hilo de escucha del cliente.
     */
    private void initInputThread() {

        activeInputThread = true;

        //es un hilo anónimo con un runnable
        inputThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int signal;

                //mientras este hilo esté activo
                while (activeInputThread) {

                    try {

                        //se lee una señal regogiendola como número entero del canal de input de bajo nivel
                        signal = readSignedInt32();

                        //se procesas la señal.
                        processInputSignal(signal);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        //se inicia el hilo
        inputThread.start();

    }

    private void processInputSignal(int signal) {

        System.out.println("señal del cliente: " + signal);

        switch (signal) {
            case VultureCamSignals.SHUTDOWN_CAMERA_TO_CAMERA_SIGNAL:
                //señal de apagado del streaming
                shutdownStreaming();
                break;
        }

    }

    /**
     * Apaga la redirección del streaming.
     */
    private void shutdownStreaming() {

        //sincroniza el hilo de input
        synchronized (ownThread) {

            //levanta la flag que apaga el flujo de frames del hilo de envío de streaming
            nextLenSignalIsShutdown = true;

            //levanta la flag que apaga el hilo del flujo de entrada de señales para mater el hilo
            activeInputThread = false;
        }

    }

    /**
     * Método que envía en clip MPEG-4 por el socket a bajo nivel.
     * @param request
     */
    private void downloadClip(ClientRequest request) {

        ClientResponse response;
        byte[] clipBytes = {};

        //se busca la grabación solicitada en la petición.
        Record record = database.findRecord(request.getClipId());

        //se instancia el fichero
        File clip = new File(record.getPath());

        try {
            //se leen todos los bytes del fichero
            clipBytes = Files.readAllBytes(clip.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //si el fichero existe y se ha podido leer
        if (clip.exists() && clipBytes.length > 0) {

            //se crea una nueva respuesta de alto nivel para enviarsela al cliente indicando el tamaño de los bytes del fichero que se va a enviar al cliente a bajo nivel
            response = new ClientResponse(true, 0, null, null, clipBytes.length);

            //se manda la respuesta de alto nivel por el socket que le indica el tamaño de lectura de socket que debe de realizar en bytes en en el próximo envío
            writeResponseInSocket(response);

            try {
                //se manda por el canal de bajo nivel en el socket los bytes del fichero MPEG-4 de la grabación para que los recepcione el cliente.
                outputLowLevel.write(clipBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            //si no se ha podido leer el fichero se devuelve uan respuesta que que informa al cliente del fallo
            response = new ClientResponse(false, 0, null, null, 0);
            writeResponseInSocket(response);

        }

    }

    /**
     * Envío de la lista de clips al cliente de una cámara.
     * @param request
     */
    private void sendListOfClips(ClientRequest request) {

        //se busca la cámara solicitada en la petición.
        Camera selectedCamera = database.findCamera(request.getCamId());

        //se recogen todos los modelos de clip de una cáara
        ArrayList<Record> listRecord = database.getCamClips(selectedCamera);

        //se mutan los modelos a unos modelos reducidos para la respuesta y se conforma la respuesta.
        ClientResponse response = new ClientResponse(true, 0, ClientResponse.ClipResponse.mutateList(listRecord), null, 0);

        //se manda por el canal de alto nivel la respuesta
        writeResponseInSocket(response);
        System.out.println("Enviando");
    }

    /**
     * Envío de la lista de cámaras a un cliente.
     */
    private void sendListOfCams() {

        //se recoge toda la lista de cámaras del usuario autentificado
        ArrayList<Camera> listCams = database.getUserCameras(user);

        //se conforma la respuesta con los modelos de cámaras mutados a unos modelos más livianos preparados para la respuesta.
        ClientResponse response = new ClientResponse(true, 0, null, ClientResponse.CamResponse.mutateList(listCams), 0);

        //se envía la respuesta de alto nivel por el socket
        writeResponseInSocket(response);
    }


    /**
     * Autentifica a un usuario en este hilo.
     * @param request
     */
    private void authUser(ClientRequest request) {

        //busca a un usuario
        user = database.findUser(request.getUser(), request.getPassword());

        //se le asigna el valor positivo si se ha encontrado y negativo si no
        isAuth = (user != null);

        //se coge una respuesta positiva o negativa
        ClientResponse response = isAuth ? STATUS_TRUE_RESPONSE : STATUS_FALSE_RESPONSE;

        //se manda la respuesta por el canal de alto nivel
        writeResponseInSocket(response);

    }

    //manda una respuesta json por el canal de alto nivel, esto lo hace serializando la respuesta a json
    private void writeResponseInSocket(ClientResponse response) {
        try {
            outputHighLevel.writeUTF(gson.toJson(response, ClientResponse.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //cierra el socket
    private void closeSocketConnection() {

        try {
            inputLowLevel.close();
            outputLowLevel.close();
            socket.close();

            System.out.println("Cerrada conexion cliente");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //método síncrono para recibir el siguiente frame de la redirección de la transmisión del streaming
    @Override
    public synchronized void nextFrame(byte[] frame) {

        this.setNextFrameBytes(frame);
        waitForNextFrame = false;
        this.notify();
    }

    @Override
    public void setCamThread(CamThread camThread) {
        this.camThread = camThread;
    }

    public synchronized void setNextFrameBytes(byte[] nextFrameBytes) {
        this.nextFrameBytes = nextFrameBytes;
    }

    //bloquea el hilo
    private synchronized void threadSleep() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] intToBytes(int num) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(num).array();
    }

    private int readSignedInt32() throws IOException {

        //se leen los siguientes 4 primeros bytes, que son los 32 bits de un numero entero
        byte[] bytes = inputLowLevel.readNBytes(Integer.BYTES);

        //se transforman los 4 bytes del signed integer, el tipo primitivo int.
        return ByteBuffer.wrap(bytes).getInt();
    }

}

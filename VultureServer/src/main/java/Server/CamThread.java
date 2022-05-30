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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;


/**
 * Hilo encargado de gestionar las conexiones de  una cámara, de ircodificando los clips MPEG-4 y manejar el sistema de ficheros.
 */
public class CamThread extends Thread {

    //duración de los clips en minutos
    private final int CLIP_DURATION_MINS = 10;

    //duración de los clips en nano segundos.
    private final long CLIP_DURATION_NANO_SECS = CLIP_DURATION_MINS * 60000000000l;

    //bytes de la longitud de un nombre de cámara enviado por el socket.
    private final int MAX_CAM_NAME_BYTES = 20;

    //caracter de relleno para un nombre de cámara incompleto.
    private final String BACKFILL_CHARACTER_STRINGS = "*";

    //escuchadores del streaming. El hilo dirigirá cada frame a los consumidores del streaming.
    private final ArrayList<StreamingListener> streamingListeners;

    //socket
    private final Socket socket;
    //controlador de la base de datos
    private final DBController database;
    //input output del socket
    private InputStream input;
    private OutputStream output;
    private VideoManager video;

    //datos usados por el hilo
    private int clientId;
    private String camName;
    private User user;
    private int camID;
    private Camera camera;

    private Record record;
    private byte[] bytes;

    //flags del estado del hilo
    private boolean active, cameraOn;

    //objetos para manejar datos temporales
    private LocalDateTime initDate, finalDate;
    private long initTime, actualTime, diffTime;

    //constructor.
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

    public void addStreamingListener(StreamingListener listener) {
        System.out.println("Streaming listener añadido correctamente");
        streamingListeners.add(listener);
    }

    public void removeStreamingListener(StreamingListener listener) {
        streamingListeners.remove(listener);
    }

    @Override
    public void run() {

        byte[] signalbytes;
        boolean isAuth = true;

        //se abre la conexión con la base de datos.
        database.openConnection();

        try {
            //se reciben las señales iniciales de la cámara que la identificarán en el sistema
            //si se lanza una AuthException es por que la cámara no se ha autentificado correctamente y se rechaza la conexión.
            reciveInitialSignals();
            refreshDataBaseState();
        } catch (AuthException e) {
            isAuth = false;
            System.out.println(e.getMessage());
        }

        //bucle de inicio de streaming constante mientras la camara esté encendida y autentificada.
        /**
         * Este bucle está preparado para pausar y reanudar el streaming de la cámara las veces que sean necesarios.
         */
        while (cameraOn && isAuth) {

            //bloqueo del hilo en cada reanudación del streaming.
            while (!active) {
                threadSleep();
            }

            //si la conexión con la base de datos está cerrada entonces de abre.
            if (database.isClosed()) {
                database.openConnection();
            }
            //se crean los bytes de la señal de inicio del streaming
            signalbytes = intToByteArray(VultureCamSignals.START_STREAMING_TO_CAMERA_SIGNAL);

            try {
                //se le mandan a la cámara a la escucha del socket para que empiece a transmitir
                output.write(signalbytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                //se inicia la escucha del streaming
                leaseStreaming();
            } catch (IOException e) {
                //se evita el problema del bucle de creacion de ficheros
                cameraOn = false;
                e.printStackTrace();
            }

            //siempre se cierra la conexión con la base de datos cuando la cámara deja de transmitir
            database.closeConnection();
        }

        //Si se ha salido del bucle por que no se ha autentificado la cámara entonces
        if (!isAuth) {
            //se le manda la señal de apagado
            shutdownStreaming();
            //se cierra la base de datos.
            if (database != null) database.closeConnection();
        }

        //se cierran las conexiones del socket
        closeSocketConnection();

        System.out.println("Cloe camera thread:" + this.getId());

    }


    /**
     * Actualzia los datos de inicio de nombre, inicio de transmisicón y id del hilo de una cámara.
     */
    private void refreshDataBaseState() {

        //se calcula el momento actual.
        LocalDateTime dateNow = LocalDateTime.now();

        //refresh camera log fields
        database.updateCamera(camera, camName, dateNow, this.getId());

    }

    /**
     * Método para recoger los primeros paquetes de la conexión que son los datos de autenticación.
     *
     * @throws AuthException
     */
    private void reciveInitialSignals() throws AuthException {

        try {

            //de primeras se lee el número de cliente al que está asociado la cámara
            clientId = readSignedInt32();

            System.out.println("Cliente nº: " + clientId);

            camID = readSignedInt32();

            System.out.println("Cámara id: " + camID);

            //se lee el nombre de la cámara con una longitud fija de 20 bytes
            camName = readString(MAX_CAM_NAME_BYTES);

            System.out.println("Cámara: " + camName);

            //se autentifican las credenciales recogidas.
            authCredentials();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Autentifica la cámara en la base de datos y que pertenece al usuario que cide pertenecer.
     *
     * @throws AuthException
     */
    private void authCredentials() throws AuthException {

        //busca al usaurio.
        user = database.findUser(clientId);

        //si se encuentra al usuario
        if (user != null) {

            //se busca la cámara
            camera = database.findCamera(user, camID);

            //si no se encuentra se lanza la exceptión
            if (camera == null) {
                throw new AuthException(-1, camID, camName);
            }

           /*
            Se comparan los ids de la clave primario del usuario y la clave foranea del usuario en la cámara si no coinciden se lanza una excepción
           */
            if (user.getId() != camera.getId_user()) {
                throw new AuthException(user.getId(), camera.getId(), camera.getName());
            }
        } else {
            throw new AuthException(clientId, -1, "");
        }

    }

    /**
     * Método que inicia el treaming de la cámara.
     */
    public synchronized void startStreaming() {
        active = true;
        this.notify();
    }

    /**
     * Nétodo que para el streaming de la cámara.
     */
    public void stopStreaming() {

        //se le envía una señal a la cámara para que deje de enviar el streaming
        try {
            if (!socket.isClosed())
                output.write(intToByteArray(VultureCamSignals.STOP_STREAMING_TO_CAMERA_SIGNAL));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Método que apaga el streaming.
     */
    public void shutdownStreaming() {

        //se le envía una señal a la cámara para que apague el sistema
        try {
            output.write(intToByteArray(VultureCamSignals.SHUTDOWN_CAMERA_TO_CAMERA_SIGNAL));
            initCloseTimeOut();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Se inicia un hilo temporizador para dar tiempo a cerrar la conexión con la cámara.
     */
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
    private void interruptThread() {
        if (active || cameraOn) {
            active = false;
            cameraOn = false;
            closeSocketConnection();
        }
    }

    /**
     * Método de escucha de lstreaming, este es el bucle principal del streaming.
     *
     * @throws IOException
     */
    private void leaseStreaming() throws IOException {

        System.out.println("Iniciada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        int bufferSizeSignal = 0;

        active = true;

        //objeto encargado de codificar el streaming en ficheros MPEG-4
        video = new VideoManager();

        //se inicia el codificador de vídeo con el clip temporal generado para esta instancia de cámara
        video.startMp4Encode(ClipHandler.generateClipTempPath(user, camera));

        //se recoge la fecha de inicio de codificación del streaming
        initDate = LocalDateTime.now();
        initTime = System.nanoTime();

        //se comienza la lectura
        while (active && cameraOn) {

            //se lee el número de bytes que va a tener el próximo frame que se envíe por el socket
            bufferSizeSignal = readSignedInt32();

            //si la señal es negativa significa que es una señal de comunicación del protocolo
            if (bufferSizeSignal < 0) {
                processSignal(bufferSizeSignal);
                //si es positiva es el tamaño del buffer en bytes por lo tanto se procede a procesar el siguietne frame
            } else {
                processFrame(bufferSizeSignal);
                checkEndClip();
            }
        }

        System.out.println("Parada la escucha el streaming de la cámara " + camName + " del cliente id " + clientId);

        //Si el clip estaba codificando se cierra y se guarda.
        if (video.isEncoding()) {
            stopAndSaveFinalClip();
        }

        active = false;

    }


    private void processSignal(int signal) {

        switch (signal) {

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
        for (StreamingListener listener : streamingListeners) {
            listener.nextFrame(bytes);
        }

        video.nextFrame(bytes);

    }

    /**
     * Este método chekea que no haya que finalizar ya la grabación del clip para iniciar otro.
     * @throws IOException
     */
    private void checkEndClip() throws IOException {

        actualTime = System.nanoTime();

        //si el tiempo de grabación del clip es superior al establecido se guarda un clip y se crea otro
        if (isClipEnd()) {

            //se para y se guarda la grabación del clip actual
            stopAndSaveFinalClip();

            //se reicia la codificación de otro clip
            video.startMp4Encode(ClipHandler.generateClipTempPath(user, camera));
            initDate = LocalDateTime.now();
            initTime = System.nanoTime();
        }
    }

    /**
     * Para a codificación del clip actual y lo guarda en una ruta definitiva.
     */
    private void stopAndSaveFinalClip() {
        //se general la fecha de fin de la grabación
        finalDate = LocalDateTime.now();

        //se para y se guarda el clip en el path tmeporal
        video.stopAndSave();

        //se crea una instancia de grabación incompleta, le falta el path definitivo
        record = new Record(initDate, finalDate, null, camera.getId());

        //se genera una ruta final para la grabación
        record = ClipHandler.moveTemFileToFinalPath(user, camera, record);

        //se reubica el fichero de la grabación almacenado en el path temporal a la ruta final.
        database.saveRecord(record);
    }

    /**
     * Método que cierra la conexión del socket
     */
    private void closeSocketConnection() {

        try {
            input.close();
            socket.close();

            System.out.println("Apagada la cámara " + camName + " del cliente id " + clientId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para bloquear el hilo
     */
    private synchronized void threadSleep() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para comparar los tiempos de la grabación y el máximo tiempo de grabación
     * @return booleano indicando si se ha sobrepasado el tiempo máximo de grabación.
     */
    private boolean isClipEnd() {

        diffTime = actualTime - initTime;
        //System.out.println("Segs: " + (diffTime / 1000000000l));

        return (diffTime >= CLIP_DURATION_NANO_SECS);
    }


    /**
     * Método que lee del input del socket un número entero de 32 bits con signo.
     * @return número entero.
     * @throws IOException
     */
    private int readSignedInt32() throws IOException {

        //se leen los siguientes 4 primeros bytes, que son los 32 bits de un numero entero
        byte[] bytes = input.readNBytes(Integer.BYTES);

        //se transforman los 4 bytes del signed integer, el tipo primitivo int.
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * Método que lee una cadena de caracteres de longitud n bytes.
     * @param nBytes número de bytes de la cadena que se van a leer del socket.
     * @return string.
     * @throws IOException
     */
    private String readString(int nBytes) throws IOException {

        String cadena;

        byte[] strBytes = input.readNBytes(nBytes);

        cadena = new String(strBytes, StandardCharsets.UTF_8);

        //Se eliminan los caracteres de relleno
        return cadena.replace(BACKFILL_CHARACTER_STRINGS, "");

    }

    /**
     * Método que cobierte un número entero de 32 bits con signo en el array de los 4 bytes que lo conforman.
     * @param num número entero.
     * @return
     */
    private byte[] intToByteArray(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    //getter para preguntar si el hilo está activo.
    public boolean isActive() {
        return active;
    }
}

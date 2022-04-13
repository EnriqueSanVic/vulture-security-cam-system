package Server;

import Stream.VideoManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class CamThread extends Thread{

    private final int CLOSE_CONNECTION_COMMAND = -2341;

    private final int MAX_CAM_NAME_BYTES = 20;

    private final String BACKFILL_CHARACTER_STRINGS = "*";

    private Socket socket;
    private InputStream input;

    private VideoManager video;

    private int clientId;
    private String camName;

    boolean active;


    public CamThread(Socket socket) {
        this.socket = socket;

        try {
            input = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        int bufferSize = 0;
        byte[] bytes;

        active = true;

        video = new VideoManager();

        try{

            video.startMp4Encode("./video.mp4");

            //de primeras se lee el número de cliente al que está asociado la cámara
            clientId = readSignedInt32();

            System.out.println("Cliente nº: " + clientId);

            //se lee el nombre de la cámara con una longitud fija de 20 bytes
            camName = readString(MAX_CAM_NAME_BYTES);

            System.out.println("Cámara: " + camName);

            //se comienza la lectura
            while(active){

                //se lee el número de bytes que va a tener el próximo frame que se envíe por el socket
                bufferSize = readSignedInt32();

                if(bufferSize != CLOSE_CONNECTION_COMMAND) {

                    System.out.println("Tamaño frame: " + bufferSize + " bytes");

                    //se hace una lectura de los siguientes n bytes para leer la imagen
                    bytes = input.readNBytes(bufferSize);

                    video.nextFrame(bytes);

                }else{
                    active = false;
                }

            }

            video.stopAndSave();

            input.close();

            socket.close();

            System.out.println("Cerrada cámara " + camName + " del cliente id " + clientId);

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private int readSignedInt32 () throws IOException {

        //se leen los siguientes 4 primeros bytes, que son los 323 bits de un numero entero
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
}

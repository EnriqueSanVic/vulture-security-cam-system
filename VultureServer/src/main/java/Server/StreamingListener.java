package Server;


/**
 * Interfáz que hace de una clase un escuchador válido al que redirigir el flujo de frames del streaming
 */
public interface StreamingListener {

    /**
     * El hilo productor del streaming invoca a este método de su escuchador/escuchadores para
     * pasarles los bytes del siguiente frame del streaming para que estos lo procesen.
     *
     * @param frame array de bytes con contienen el frame codificado en JPEG.
     */
    void nextFrame(byte[] frame);

    /**
     * Permite dar a los escuchadores la referencia el hilo productor del streaming para poder realizar llamadas bidireccionales.
     *
     * @param camThread Hilo productor del streaming.
     */
    void setCamThread(CamThread camThread);
}

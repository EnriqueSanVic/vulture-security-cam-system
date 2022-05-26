package Server;

public interface StreamingListener {

    void nextFrame(byte[] frame);

    void setCamThread(CamThread camThread);
}

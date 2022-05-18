package Server;

public class ClientApiThread implements StreamingListener{

    private CamThread camThread;

    private byte[] nextFrameBytes;


    @Override
    public void nextFrame(byte[] frame) {
        synchronized(this){
            this.setNextFrameBytes(frame);
            this.notify();
        }
    }

    @Override
    public void setCamThread(CamThread camThread) {
        this.camThread = camThread;
    }

    public void setNextFrameBytes(byte[] nextFrameBytes) {
        this.nextFrameBytes = nextFrameBytes;
    }


}

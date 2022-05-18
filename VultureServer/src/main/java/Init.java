import Server.ServerConnectionsManager;
import Views.CamView;

public class Init {

    public static void main(String[] args){

        //init the control panel
        CamView.getInstance();

        /*

        server.start();

        server.setStreamingListenerToCamThread(CamView.getInstance(),23l);

        */

    }



}

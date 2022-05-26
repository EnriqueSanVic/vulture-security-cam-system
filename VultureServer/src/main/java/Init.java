import Models.ClientRequest;
import Server.ServerCamConnectionsHandler;
import Server.ServerClientConnectionsHandler;
import Views.CamView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Init {

    public static void main(String[] args){

        //init the control panel
        //CamView.getInstance();


        ServerCamConnectionsHandler servCam = new ServerCamConnectionsHandler();

        servCam.start();

        ServerClientConnectionsHandler servCli = new ServerClientConnectionsHandler(servCam);

        servCli.start();

        /*

        server.start();

        server.setStreamingListenerToCamThread(CamView.getInstance(),23l);

        */



    }



}

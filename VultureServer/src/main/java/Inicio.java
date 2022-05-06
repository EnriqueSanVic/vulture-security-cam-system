import Server.ServerConnectionsManager;
import Views.CamView;

public class Inicio {

    public static void main(String[] args){

        CamView.getInstance();

        new ServerConnectionsManager();

    }



}

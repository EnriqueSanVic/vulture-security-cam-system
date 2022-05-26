package Views.Controllers;

import Database.DBController;
import Models.Camera;
import Models.User;
import Server.ServerCamConnectionsHandler;
import Views.CamView;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class CamViewControler extends WindowAdapter implements ListSelectionListener, ActionListener {

    private CamView view;

    private ServerCamConnectionsHandler server;
    private StreamingViewProcessor streamingViewProcessor;

    private User user;
    private Camera camera;

    private DBController database;
    private ArrayList<User> userList;
    private ArrayList<Camera> cameraList;

    public CamViewControler(CamView view, StreamingViewProcessor streamingViewProcessor) {
        this.view = view;
        this.streamingViewProcessor = streamingViewProcessor;
        this.database = new DBController();
        this.database.openConnection();
        this.server = new ServerCamConnectionsHandler();
    }

    public void windowClosing(WindowEvent e){
        synchronized (streamingViewProcessor){
            //stop processor thread
            streamingViewProcessor.setActive(false);
            streamingViewProcessor.notify();
        }

        view.dispose();
    }


    public void reloadUserList(){

        userList = database.getAllUsers();

        view.setUserListData(userList.toArray(new User[0]));

    }

    public void loadCameraList(){

        cameraList = database.getUserCameras(user);

        view.setCameraListData(cameraList.toArray(new Camera[0]));

    }


    @Override
    public void valueChanged(ListSelectionEvent e) {

        if(e.getSource() == view.getUserList()){
            processUserSelected();
        }else if(e.getSource() == view.getCameraList()){
            processCameraSelected();
        }

    }

    private void processUserSelected() {
        user = view.getUserSelected();
        if(user != null){
            loadCameraList();
        }
    }

    private void processCameraSelected() {

        Camera cameraSelected = view.getCameraSelected();

        if(cameraSelected != camera){
            removePreviousCameraStreaming();
            camera = cameraSelected;

            refreshCameraData();

            if(camera != null){
                loadCameraStreaming();
            }
        }

    }



    private void removePreviousCameraStreaming() {
        if(camera != null){

            long refThread = camera.getRef_hilo();

            if(refThread != 0l){

                boolean correctInit = server.removeStreamingListenerToCamThread(view.getStreamingViewProcessor(), refThread);

                if(correctInit){
                    System.out.println("Dejando de escuchar el streaming correctamente.");
                }else{
                    System.out.println("La cámara no está transmitiendo.");
                }
            }
        }
    }

    //intenta cargar la cámara en el procesador de streaming se la vista
    private void loadCameraStreaming() {

        if(camera != null){

            long refThread = camera.getRef_hilo(); //se recoge la referencia del hilo de la cámara seleccionada

            if(refThread != 0l){ //si la referencia existe

                //se intenta añadir el procesador de streaming se la vista al hilo de la cámara
                boolean correctInit = server.setStreamingListenerToCamThread(view.getStreamingViewProcessor(), refThread);

                //si el acople ha sido correcto se marca como tal
                if(correctInit){
                    System.out.println("Escuchando streaming correctamente.");
                }else{
                    System.out.println("La cámara no está transmitiendo.");
                }
            }

        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        System.out.println(e.getActionCommand());
        switch (e.getActionCommand()) {

            case CamView.CHANGE_STATE_OF_SERVER_COMMAND:
                changeServerState();
                break;

            case CamView.CHANGE_REFRESH_CAMERA_SELECTED_COMMAND:
                refreshCameraData();
                loadCameraStreaming();
            break;

            case CamView.CAMERA_BUTTON_COMMAND:

                startStopStreaming();
                break;
        }
    }

    private void startStopStreaming() {

        if(server!=null && server.isActive()){
            if(server.isCamActive(camera.getRef_hilo())){
                server.stopStreaming(camera.getRef_hilo());
                view.changeStartStopButtonState(false);
            }else{
                server.startStreaming(camera.getRef_hilo());
                view.changeStartStopButtonState(true);
            }
        }
    }

    private void refreshCameraData() {

        if(camera != null){

            Camera tempCamera = database.findCamera(camera.getId());

            if(tempCamera != null){
                camera = tempCamera;
                refeshCameraViewInfo();
            }
        }

    }

    private void refeshCameraViewInfo() {

        view.changeStartStopButtonState(server.isCamActive(camera.getRef_hilo()));

        view.setCameraId(String.valueOf(camera.getId()));
        view.setCameraName(String.valueOf(camera.getName()));
        view.setCameraLastTransmission(camera.getLast_transmission().toString().replace("T", " "));

    }

    private void changeServerState() {

        if(server == null || !server.isActive()){
            server = new ServerCamConnectionsHandler();
            server.start();
            view.changeBtnServerStateAppearance(true);
            view.setListsEnabled(true);
            reloadUserList();

        }else{
            server.shutdownAllStreamings();
            view.changeBtnServerStateAppearance(false);
            view.setListsEnabled(false);
            view.cleanUserList();
            view.clearCamList();
            view.clearCamFields();
            user = null;
            camera = null;
        }

    }

}

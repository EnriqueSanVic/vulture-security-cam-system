package Views;

import Models.Camera;
import Models.User;
import Views.Controllers.CamViewControler;
import Views.Controllers.StreamingViewProcessor;
import Views.ImageUtils.Icon32;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

public class CamView extends JFrame {


    public static final String CHANGE_STATE_OF_SERVER_COMMAND = "C*anGheSt_Te";
    public static final String CHANGE_REFRESH_CAMERA_SELECTED_COMMAND = "Re*_Res$_CAmera_Selected";
    public static final String CAMERA_BUTTON_COMMAND = "Cam_StartOrStop";

    private static Font CARME_FONT;

    //read font from resources
    static{

        InputStream is = CamView.class.getClassLoader().getResourceAsStream("Fonts/Carme.ttf");
        try {
            CARME_FONT = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Font carmeTitleFont, carmeListFont, carmeHeadFont, carmeBodyFont, carmeStaticBodyFont;

    private final int WIDTH = 1300, HEIGHT = 660;
    private final int WIDTH_STREAMING_AREA= 640, HEIGHT_STREAMING_AREA = 480;
    private final int X_STREAMING_AREA = 600, Y_STREAMING_AREA = 107;
    private final Color BACKGROUND_COLOR = new Color(85, 85, 85);
    private final Color FOCUS_COLOR = Color.WHITE;
    private final Color FOCUS_FOREGROUND_COLOR = Color.BLACK;

    private final Font LIST_FONT = new Font("Serif", Font.PLAIN, 17);
    private final Border LIST_BORDER = BorderFactory.createLineBorder(Color.white);

    private CamViewControler controller;
    private StreamingViewProcessor streamingViewProcessor;


    private ImageIcon btnBlue;
    private ImageIcon btnDarkBlue;
    private ImageIcon btnReload;


    private Canvas streamingArea;

    private JLabel serverStateLabel;
    private JLabel headBanner;
    private JLabel usersLabel;
    private JLabel camerasLabel;
    private JLabel streamingLabel;

    private JPanel camGroupPanel;

    private JLabel camIdStaticLabel;
    private JLabel camIdLabel;
    private JLabel camNameStaticLabel;
    private JLabel camNameLabel;
    private JLabel camLastTransmissionDateStaticLabel;
    private JLabel camLastTransmissionDateLabel;

    private JButton serverStateButton;
    private JButton reloadCameraSelectedButton;
    private JButton startStopButton;

    private JList<User> userList;
    private JList<Camera> cameraList;

    private DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer(){
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (isSelected) {
                c.setBackground(FOCUS_COLOR);
                c.setForeground(FOCUS_FOREGROUND_COLOR);
                c.setFont(carmeListFont);
            }else{
                c.setBackground(FOCUS_FOREGROUND_COLOR);
                c.setForeground(FOCUS_COLOR);
                c.setFont(carmeListFont);
            }
            return c;
        }
    };

    private static CamView singelton = null;

    public static CamView getInstance(){

        if(singelton == null){
            singelton = new CamView();
        }

        return singelton;
    }

    private CamView(){

        super();

        confFonts();

        confWindow();

        confComponents();

        initialState();

        centerWindow();

        this.setVisible(true);

        streamingViewProcessor = new StreamingViewProcessor(streamingArea);

        controller = new CamViewControler(this, streamingViewProcessor);

        confListeners();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                streamingViewProcessor.start();
            }
        });

    }

    private void initialState() {

        changeBtnServerStateAppearance(false);
        setListsEnabled(false);
    }

    private void confFonts() {

        carmeTitleFont = CARME_FONT.deriveFont(Font.BOLD, 26);
        carmeListFont = CARME_FONT.deriveFont(Font.BOLD, 13);
        carmeHeadFont = CARME_FONT.deriveFont(Font.BOLD, 15);
        carmeStaticBodyFont = CARME_FONT.deriveFont(Font.BOLD, 18);
        carmeBodyFont = CARME_FONT.deriveFont(Font.ITALIC, 18);
    }

    private void confWindow() {

        this.setSize(WIDTH, HEIGHT);

        this.getContentPane().setBackground(BACKGROUND_COLOR);

        this.setLayout(null);

        this.setResizable(false);

        this.setTitle("Vulture Server");

        try {
            this.setIconImage(ImageIO.read(getClass().getClassLoader().getResource("Assets/AppIcon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void centerWindow() {

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

    }

    private void confListeners() {

        userList.addListSelectionListener(controller);

        cameraList.addListSelectionListener(controller);

        serverStateButton.addActionListener(controller);

        reloadCameraSelectedButton.addActionListener(controller);

        startStopButton.addActionListener(controller);
    }

    private void confComponents() {

        confServerStateLabel();
        this.add(serverStateLabel);

        confServerStateButton();
        this.add(serverStateButton);

        confHeadBanner();
        this.add(headBanner);

        confUsersLabel();
        this.add(usersLabel);

        confCamerasLabel();
        this.add(camerasLabel);

        confStreamingLabel();
        this.add(streamingLabel);

        confUserList();
        this.add(userList);

        confCamList();
        this.add(cameraList);

        confStreamingArea();
        this.add(streamingArea);

        confReloadCameraSelectedButton();
        this.add(reloadCameraSelectedButton);

        confCamInfoLabelGroup();

        this.add(camIdStaticLabel);
        this.add(camIdLabel);
        this.add(camNameStaticLabel);
        this.add(camNameLabel);
        this.add(camLastTransmissionDateStaticLabel);
        this.add(camLastTransmissionDateLabel);

        confStartStopButton();
        this.add(startStopButton);

        confCamGroupPanel();
        this.add(camGroupPanel);

    }

    private void confStartStopButton() {

        startStopButton = new JButton("START");

        startStopButton.setBounds(465, 550, 80, 25);

        startStopButton.setActionCommand(CAMERA_BUTTON_COMMAND);

    }

    public void changeStartStopButtonState(boolean active){
        if(active){
            startStopButton.setText("START");
        }else{
            startStopButton.setText("STOP");
        }
    }

    private void confCamGroupPanel() {

        camGroupPanel = new JPanel();

        camGroupPanel.setBounds(50, 420, 512, 170);

        camGroupPanel.setBackground(Color.DARK_GRAY);

        camGroupPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

    }

    private void confCamInfoLabelGroup() {

        camIdStaticLabel = new JLabel("Camera id :");
        camIdLabel = new JLabel();
        camNameStaticLabel = new JLabel("Name :");
        camNameLabel = new JLabel();
        camLastTransmissionDateStaticLabel = new JLabel("Last transmission start :");
        camLastTransmissionDateLabel = new JLabel();

        camIdStaticLabel.setFont(carmeStaticBodyFont);
        camIdLabel.setFont(carmeBodyFont);
        camNameStaticLabel.setFont(carmeStaticBodyFont);
        camNameLabel.setFont(carmeBodyFont);
        camLastTransmissionDateStaticLabel.setFont(carmeStaticBodyFont);
        camLastTransmissionDateLabel.setFont(carmeBodyFont);

        camIdStaticLabel.setForeground(Color.WHITE);
        camIdLabel.setForeground(Color.WHITE);
        camNameStaticLabel.setForeground(Color.WHITE);
        camNameLabel.setForeground(Color.WHITE);
        camLastTransmissionDateStaticLabel.setForeground(Color.WHITE);
        camLastTransmissionDateLabel.setForeground(Color.WHITE);

        camIdStaticLabel.setBounds(80, 430, 100, 25);
        camIdLabel.setBounds(196, 430, 100, 25);

        camNameStaticLabel.setBounds(80, 470, 100, 25);
        camNameLabel.setBounds(165, 470, 100, 25);

        camLastTransmissionDateStaticLabel.setBounds(80, 510, 250, 25);
        camLastTransmissionDateLabel.setBounds(315, 510, 250, 25);

    }

    public void setCameraId(String id){
        camIdLabel.setText(id);
    }

    public void setCameraName(String name){
        camNameLabel.setText(name);
    }

    public void setCameraLastTransmission(String lastTransmission){
        camLastTransmissionDateLabel.setText(lastTransmission);
    }

    private void confReloadCameraSelectedButton() {

        reloadCameraSelectedButton = new JButton();

        reloadCameraSelectedButton.setSize(35, 35);

        reloadCameraSelectedButton.setLocation(515, 430);

        try {
            btnReload = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("Assets/btnReload.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadCameraSelectedButton.setHorizontalTextPosition(SwingConstants.CENTER);
        reloadCameraSelectedButton.setActionCommand(CHANGE_REFRESH_CAMERA_SELECTED_COMMAND);
        reloadCameraSelectedButton.setOpaque(false);
        reloadCameraSelectedButton.setBorderPainted(false);
        reloadCameraSelectedButton.setFocusPainted(false);
        reloadCameraSelectedButton.setContentAreaFilled(false);

        reloadCameraSelectedButton.setIcon(btnReload);


        reloadCameraSelectedButton.addMouseListener(new MouseAdapter() {

            private final int HUNDIMIENTO_BOTON = 5;


            @Override
            public void mousePressed(MouseEvent e) {
                reloadCameraSelectedButton.setLocation(reloadCameraSelectedButton.getLocation().x, reloadCameraSelectedButton.getLocation().y + HUNDIMIENTO_BOTON);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                reloadCameraSelectedButton.setLocation(reloadCameraSelectedButton.getLocation().x, reloadCameraSelectedButton.getLocation().y - HUNDIMIENTO_BOTON);
            }
        });

    }

    private void confServerStateButton() {

        serverStateButton = new JButton();

        serverStateButton.setFont(carmeListFont);

        serverStateButton.setForeground(Color.WHITE);

        serverStateButton.setSize(60, 25);

        serverStateButton.setLocation(125, 15);

        btnBlue = new Icon32(getClass().getClassLoader().getResource("Assets/btnBlue.png"));
        btnDarkBlue = new Icon32(getClass().getClassLoader().getResource("Assets/btnDarkBlue.png"));

        serverStateButton.setHorizontalTextPosition(SwingConstants.CENTER);
        serverStateButton.setActionCommand(CHANGE_STATE_OF_SERVER_COMMAND);
        serverStateButton.setOpaque(false);
        serverStateButton.setBorderPainted(false);
        serverStateButton.setFocusPainted(false);
        serverStateButton.setContentAreaFilled(false);

        serverStateButton.addMouseListener(new MouseAdapter() {

            private final int HUNDIMIENTO_BOTON = 5;


            @Override
            public void mousePressed(MouseEvent e) {
                serverStateButton.setLocation(serverStateButton.getLocation().x, serverStateButton.getLocation().y + HUNDIMIENTO_BOTON);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                serverStateButton.setLocation(serverStateButton.getLocation().x, serverStateButton.getLocation().y - HUNDIMIENTO_BOTON);
            }
        });

    }

    public void changeBtnServerStateAppearance(boolean active){
        if(active){
            serverStateButton.setIcon(btnBlue);
            serverStateButton.setForeground(Color.BLACK);
            serverStateButton.setText("ON");
        }else{
            serverStateButton.setIcon(btnDarkBlue);
            serverStateButton.setForeground(Color.WHITE);
            serverStateButton.setText("OFF");
        }
    }

    private void confServerStateLabel() {

        serverStateLabel = new JLabel("Server");

        serverStateLabel.setFont(carmeHeadFont);

        serverStateLabel.setForeground(Color.WHITE);

        serverStateLabel.setSize(140, 35);

        serverStateLabel.setLocation(70, 10);
    }

    private void confStreamingLabel() {

        streamingLabel = new JLabel("Streaming");

        streamingLabel.setFont(carmeTitleFont);

        streamingLabel.setForeground(Color.WHITE);

        streamingLabel.setSize(140, 35);

        streamingLabel.setLocation(874, 70);

    }

    private void confCamerasLabel() {

        camerasLabel = new JLabel("Cameras");

        camerasLabel.setFont(carmeTitleFont);

        camerasLabel.setForeground(Color.WHITE);

        camerasLabel.setSize(140, 20);

        camerasLabel.setLocation(378, 72);

    }

    private void confUsersLabel() {

        usersLabel = new JLabel("Users");

        usersLabel.setFont(carmeTitleFont);

        usersLabel.setForeground(Color.WHITE);

        usersLabel.setSize(100, 20);

        usersLabel.setLocation(144, 72);


    }

    private void confHeadBanner() {

        headBanner = new JLabel();

        ImageIcon image = new Icon32(getClass().getClassLoader().getResource("Assets/Cabecera.png"));

        headBanner.setIcon(image);

        headBanner.setSize(image.getIconWidth(),image.getIconHeight());
        headBanner.setLocation(0,0);

    }

    private void confCamList() {

        cameraList = new JList<Camera>();

        cameraList.setSize(250, 310);
        cameraList.setLocation(310, 100);

        cameraList.setBackground(Color.BLACK);
        cameraList.setForeground(Color.WHITE);
        cameraList.setFont(carmeListFont);
        cameraList.setBorder(LIST_BORDER);

        cameraList.setCellRenderer(cellRenderer);

        DefaultListCellRenderer renderer = (DefaultListCellRenderer) cameraList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        cameraList.setFont(LIST_FONT);

    }

    private void confUserList() {
        userList = new JList<User>();

        userList.setSize(250, 310);
        userList.setLocation(50, 100);

        userList.setBackground(Color.BLACK);
        userList.setForeground(Color.WHITE);
        userList.setFont(carmeListFont);
        userList.setBorder(LIST_BORDER);

        userList.setCellRenderer(cellRenderer);

        DefaultListCellRenderer renderer = (DefaultListCellRenderer) userList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);


        userList.setFont(LIST_FONT);
    }

    private void confStreamingArea() {

        streamingArea = new Canvas();

        streamingArea.setSize(WIDTH_STREAMING_AREA, HEIGHT_STREAMING_AREA);

        streamingArea.setLocation(X_STREAMING_AREA, Y_STREAMING_AREA);

        streamingArea.setBackground(Color.darkGray);

    }

    public void setListsEnabled(boolean active){
        userList.setEnabled(active);
        cameraList.setEnabled(active);
    }

    public void setUserListData(User[] userListData){
        userList.setListData(userListData);
    }

    public void setCameraListData(Camera[] cameraListData){
        cameraList.setListData(cameraListData);
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public int getWIDTH_STREAMING_AREA() {
        return WIDTH_STREAMING_AREA;
    }

    public int getHEIGHT_STREAMING_AREA() {
        return HEIGHT_STREAMING_AREA;
    }

    public StreamingViewProcessor getStreamingViewProcessor() {
        return streamingViewProcessor;
    }

    public Object getUserList() {
        return userList;
    }

    public Object getCameraList() {
        return cameraList;
    }

    public User getUserSelected(){
        return userList.getSelectedValue();
    }

    public Camera getCameraSelected(){
        return cameraList.getSelectedValue();
    }

    public void cleanUserList(){
        userList.clearSelection();
        userList.setListData(new User[]{});
    }

    public void clearCamList(){
        cameraList.clearSelection();
        cameraList.setListData(new Camera[]{});
    }


    public void clearCamFields() {

        camNameLabel.setText("");
        camIdLabel.setText("");
        camLastTransmissionDateLabel.setText("");

    }
}


class RoundedBorder implements Border {

    private int radius;


    RoundedBorder(int radius) {
        this.radius = radius;
    }


    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
    }


    public boolean isBorderOpaque() {
        return true;
    }


    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawRoundRect(x, y, width-1, height-1, radius, radius);
    }
}



package Database;

import Models.Camera;
import Models.Record;
import Models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class DBController {

    public static DateTimeFormatter SQL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private final String CONNEXION_DRIVER = "jdbc:mariadb://localhost:3308";
    private final String USER = "root";
    private final String PASSWORD = "password";
    private final String DB_NAME = "vulture";

    private Connection connection;

    public DBController() {

    }

    public boolean isClosed(){
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public User findUser(int id){

        final String QUERY = "SELECT * FROM usuarios WHERE id=" + id;

        User user = null;
        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                user = new User(res.getInt(1), res.getString(2), res.getString(3));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public User findUser(String mail, String password){

        final String QUERY = "SELECT * FROM usuarios WHERE mail='" + mail + "' and password='" + password + "'";

        User user = null;
        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                user = new User(res.getInt(1), res.getString(2), res.getString(3));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public Camera findCamera(User user, long id){

        final String QUERY = "SELECT * FROM camaras WHERE id=" + id + " and id_usuario=" + user.getId();

        Camera cam = null;
        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                cam = new Camera(res.getLong(1), res.getLong(2), res.getString(3), res.getTimestamp(4).toLocalDateTime(), res.getLong(5));
            }

        }catch(SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cam;
    }

    public Camera findCamera(long id){

        final String QUERY = "SELECT * FROM camaras WHERE id=" + id;

        Camera cam = null;
        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                cam = new Camera(res.getLong(1), res.getLong(2), res.getString(3), res.getTimestamp(4).toLocalDateTime(), res.getLong(5));
            }

        }catch(SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cam;
    }

    public void openConnection(){
        try {
            connection = DriverManager.getConnection(CONNEXION_DRIVER,USER,PASSWORD);
            connection.setCatalog(DB_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createCamera(User user, String camName) {

        final String INSERT = "INSERT INTO camaras VALUES(NULL, " + user.getId() + ", " + camName + ", null" + ", null" + ")";

        Camera cam = null;
        Statement state = null;
        boolean result = false;

        try{

            state = connection.createStatement();

            result = state.execute(INSERT);

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;

    }

    public void saveRecord(Record record) {

        String fechaInicio = SQL_DATETIME_FORMATTER.format(record.getFechaInicio());

        String fechaFin = SQL_DATETIME_FORMATTER.format(record.getFechaFin());

        final String INSERT = "INSERT INTO grabaciones VALUES(NULL, '" + fechaInicio + "', '" + fechaFin + "', '" + normalizePathToDataBaseFormat(record.getPath()) + "', " + record.getId_camara() + ")";

        try{

            Statement state = connection.createStatement();

            state.execute(INSERT);
        }catch (SQLException ex){
            ex.printStackTrace();
        }

    }


    public void updateCamera(Camera camera, String name, LocalDateTime date, long threadId) {

        String dateTimeSQLNow = SQL_DATETIME_FORMATTER.format(date);

        final String UPDATE = "UPDATE camaras SET nombre='" + name + "', ultimo_inicio_transmision = '" + dateTimeSQLNow + "', ref_hilo=" + threadId;

        try{

            Statement state = connection.createStatement();

            state.execute(UPDATE);
        }catch (SQLException ex){
            ex.printStackTrace();
        }

    }

    private String normalizePathToDataBaseFormat (String path){
        return path.replace("\\","\\\\");
    }

    private String normalizePathFromDataBaseFormat (String path){
        return path.replace("\\\\","\\");
    }

    public ArrayList<User> getAllUsers() {

        ArrayList<User> users = new ArrayList<User>();

        final String QUERY = "SELECT * FROM usuarios";

        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                users.add(new User(res.getLong(1), res.getString(2), res.getString(3)));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;

    }

    public ArrayList<Camera> getUserCameras(User user) {

        ArrayList<Camera> cameras = new ArrayList<Camera>();

        final String QUERY = "SELECT * FROM camaras " +
                "INNER JOIN usuarios ON camaras.id_usuario = usuarios.id " +
                "WHERE usuarios.id = " + user.getId();

        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            while(res.next()){
                cameras.add(new Camera(res.getLong(1), res.getLong(2), res.getString(3), res.getTimestamp(4).toLocalDateTime(), res.getLong(5)));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cameras;

    }


    public ArrayList<Record> getCamClips(Camera camera) {

        ArrayList<Record> records = new ArrayList<Record>();

        final String QUERY = "SELECT * FROM grabaciones " +
                "INNER JOIN camaras ON grabaciones.id_camara = camaras.id " +
                "WHERE camaras.id = " + camera.getId();

        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            while(res.next()){
                records.add(new Record(res.getLong(1), res.getTimestamp(2).toLocalDateTime(), res.getTimestamp(3).toLocalDateTime(), res.getString(4), res.getLong(5)));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;


    }

    public Record findRecord(long id) {

        final String QUERY = "SELECT * FROM grabaciones WHERE id=" + id;

        Record cam = null;
        Statement state = null;

        try{

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                cam = new Record(res.getLong(1), res.getTimestamp(2).toLocalDateTime(),res.getTimestamp(3).toLocalDateTime(), res.getString(4), res.getLong(5));
            }

        }catch(SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cam;

    }
}

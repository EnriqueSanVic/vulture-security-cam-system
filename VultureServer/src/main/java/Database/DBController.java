package Database;

import Models.Camera;
import Models.User;

import java.sql.*;

public class DBController {

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

        try{

            Statement state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                user = new User(res.getInt(1), res.getString(2), res.getString(3));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        return user;
    }

    public Camera findCamera(User user, int id){

        final String QUERY = "SELECT * FROM cameras WHERE id=" + id + " and id_usuario=" + user.getId();

        Camera cam = null;

        try{

            Statement state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if(res.next()){
                cam = new Camera(res.getInt(1), res.getInt(2), res.getString(3), res.getDate(4), res.getInt(5));
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
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

    public void createCamera(User user, int camID, String camName) {



    }
}

package Database;

import Models.Camera;
import Models.Record;
import Models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Clase encargada de manejar las conexiones con la base de datos.
 * Esta clase tiene métodos para realizar consultas, actualizaciones, borrados etc...
 */
public class DBController {

    //objeto formateador de DateTime para realizar insterciones en la base de datos
    public static DateTimeFormatter SQL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private final String CONNEXION_DRIVER = "jdbc:mariadb://localhost:3308";
    private final String USER = "root";
    private final String PASSWORD = "password";
    private final String DB_NAME = "vulture";

    private Connection connection;

    public DBController() {

    }

    //méotod para ver si una conexión está cerrada
    public boolean isClosed() {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Método para buscar un usuario por su iden la trabla usuarios.
     *
     * @param id Id del usuario.
     * @return user | null.
     */
    public User findUser(int id) {

        final String QUERY = "SELECT * FROM usuarios WHERE id=" + id;

        User user = null;
        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if (res.next()) {
                user = new User(res.getInt(1), res.getString(2), res.getString(3));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Método para buscar a un usuario en la base de datos por su mail y contraseña.
     *
     * @param mail     Mail del usuario.
     * @param password Mail del usuuario.
     * @return user | null.
     */
    public User findUser(String mail, String password) {

        final String QUERY = "SELECT * FROM usuarios WHERE mail='" + mail + "' and password='" + password + "'";

        User user = null;
        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if (res.next()) {
                user = new User(res.getInt(1), res.getString(2), res.getString(3));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Méotodo para pasar una cámara pertieneciente a un usuario por su id.
     * Esto se hace para asegurar el acceso a las cámaras y que no de lugar a
     * que hilos de otros usuarios puedan acceder a cámaras que no son suyas.
     *
     * @param user usuario.
     * @param id   id de la cámara.
     * @return cámara | null.
     */
    public Camera findCamera(User user, long id) {

        final String QUERY = "SELECT * FROM camaras WHERE id=" + id + " and id_usuario=" + user.getId();

        Camera cam = null;
        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if (res.next()) {
                cam = new Camera(res.getLong(1), res.getLong(2), res.getString(3), res.getTimestamp(4).toLocalDateTime(), res.getLong(5));
            }

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cam;
    }

    /**
     * Busca una cámara por su id, este método nunca se usa para el acceso desde los
     * hilso clientes solo para el acceso del panel de administraicón en el cual no puede haber accesos no permitidos.
     *
     * @param id id de la cámara.
     * @return cámara | null.
     */
    public Camera findCamera(long id) {

        final String QUERY = "SELECT * FROM camaras WHERE id=" + id;

        Camera cam = null;
        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if (res.next()) {
                cam = new Camera(res.getLong(1), res.getLong(2), res.getString(3), res.getTimestamp(4).toLocalDateTime(), res.getLong(5));
            }

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cam;
    }

    /**
     * Método para abrir una conexión con la base de datos.
     */
    public void openConnection() {
        try {
            connection = DriverManager.getConnection(CONNEXION_DRIVER, USER, PASSWORD);
            connection.setCatalog(DB_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para cerrar a conexión con la base de datos.
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para crea una nueva cámara en la base de datos.
     *
     * @param user    usuario al que pertenecerá la cámara.
     * @param camName nombre de la cámara.
     * @return booleano que indica si se ha logrado crear la cámara.
     */
    public boolean createCamera(User user, String camName) {

        final String INSERT = "INSERT INTO camaras VALUES(NULL, " + user.getId() + ", " + camName + ", null" + ", null" + ")";

        Camera cam = null;
        Statement state = null;
        boolean result = false;

        try {

            state = connection.createStatement();

            result = state.execute(INSERT);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;

    }

    /**
     * Método para guardar una nueva grabación de una cámara.
     *
     * @param record grabación.
     */
    public void saveRecord(Record record) {

        String fechaInicio = SQL_DATETIME_FORMATTER.format(record.getFechaInicio());

        String fechaFin = SQL_DATETIME_FORMATTER.format(record.getFechaFin());

        final String INSERT = "INSERT INTO grabaciones VALUES(NULL, '" + fechaInicio + "', '" + fechaFin + "', '" + normalizePathToDataBaseFormat(record.getPath()) + "', " + record.getId_camara() + ")";

        try {

            Statement state = connection.createStatement();

            state.execute(INSERT);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }


    /**
     * Método para actualizar los datos de una cámara como los son el nombre, la fecha de inicio de transmisión y el hilo que controla la cámara.
     *
     * @param camera   camara.
     * @param name     nombre.
     * @param date     fecha de inicio de transmisión.
     * @param threadId id del hilo java que controla la cámara.
     */
    public void updateCamera(Camera camera, String name, LocalDateTime date, long threadId) {

        String dateTimeSQLNow = SQL_DATETIME_FORMATTER.format(date);

        final String UPDATE = "UPDATE camaras SET nombre='" + name + "', ultimo_inicio_transmision = '" + dateTimeSQLNow + "', ref_hilo=" + threadId;

        try {

            Statement state = connection.createStatement();

            state.execute(UPDATE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Método para escapar los caracteres \ en una routa de un fichero para que se puedan guardar en la base de datos.
     *
     * @param path path.
     * @return path normalizado.
     */
    private String normalizePathToDataBaseFormat(String path) {
        return path.replace("\\", "\\\\");
    }

    /**
     * Método para realizar la operación inversa de normalizar los caracteres de una ruta de ficheros para extrarlos de la base de datos.
     *
     * @param path path.
     * @return path.
     */
    private String normalizePathFromDataBaseFormat(String path) {
        return path.replace("\\\\", "\\");
    }

    /**
     * Método para obtener todos los usuarios de la aplicación.
     *
     * @return array de usuarios.
     */
    public ArrayList<User> getAllUsers() {

        ArrayList<User> users = new ArrayList<User>();

        final String QUERY = "SELECT * FROM usuarios";

        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if (res.next()) {
                users.add(new User(res.getLong(1), res.getString(2), res.getString(3)));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;

    }

    /**
     * Método para obtener todas las cámaras de un usuario.
     *
     * @param user usario.
     * @return Lista de cámaras de un usuario.
     */
    public ArrayList<Camera> getUserCameras(User user) {

        ArrayList<Camera> cameras = new ArrayList<Camera>();

        final String QUERY = "SELECT * FROM camaras " +
                "INNER JOIN usuarios ON camaras.id_usuario = usuarios.id " +
                "WHERE usuarios.id = " + user.getId();

        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            while (res.next()) {
                cameras.add(new Camera(res.getLong(1), res.getLong(2), res.getString(3), res.getTimestamp(4).toLocalDateTime(), res.getLong(5)));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cameras;

    }

    /**
     * Método para obtener la lista de grabaciones de una cámara.
     *
     * @param camera camara.
     * @return array de grabaciones.
     */
    public ArrayList<Record> getCamClips(Camera camera) {

        ArrayList<Record> records = new ArrayList<Record>();

        final String QUERY = "SELECT * FROM grabaciones " +
                "INNER JOIN camaras ON grabaciones.id_camara = camaras.id " +
                "WHERE camaras.id = " + camera.getId();

        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            while (res.next()) {
                records.add(new Record(res.getLong(1), res.getTimestamp(2).toLocalDateTime(), res.getTimestamp(3).toLocalDateTime(), res.getString(4), res.getLong(5)));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;


    }

    /**
     * Método para buscar una grabación por su id.
     *
     * @param id id de la grabación.
     * @return grabacion | null.
     */
    public Record findRecord(long id) {

        final String QUERY = "SELECT * FROM grabaciones WHERE id=" + id;

        Record cam = null;
        Statement state = null;

        try {

            state = connection.createStatement();

            ResultSet res = state.executeQuery(QUERY);

            res.beforeFirst();

            if (res.next()) {
                cam = new Record(res.getLong(1), res.getTimestamp(2).toLocalDateTime(), res.getTimestamp(3).toLocalDateTime(), res.getString(4), res.getLong(5));
            }

        } catch (SQLException | NullPointerException ex) {
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

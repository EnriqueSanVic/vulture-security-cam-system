package FileSaveSystem;

import Models.Camera;
import Models.Record;
import Models.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Clase de métodos estáticos relacionados con el manejo de ficheros del storage.
 */
public class ClipHandler {

    public static DateTimeFormatter FILE_NAME_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);
    private static final String BASE_PATH = "./storage";

    /**
     * Mueve un registro temporal a path final en la carpeta de uan cámara.
     *
     * @param user             usuario.
     * @param camera           cámara.
     * @param incompleteRecord grabación temporal.
     * @return grabación final.
     */
    public static Record moveTemFileToFinalPath(User user, Camera camera, Record incompleteRecord) {

        Path tempPath = new File(generateClipTempPath(user, camera)).toPath();

        Path finalPath = Paths.get(generateClipFinalPath(user, camera, incompleteRecord));

        try {
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Clip guardado en: " + finalPath);

        return new Record(incompleteRecord, finalPath.toString());

    }

    /**
     * Genera un path final para una grabación.
     *
     * @param user   usuario.
     * @param camera cámara.
     * @param record grabación temporal.
     * @return grabación final.
     */
    private static String generateClipFinalPath(User user, Camera camera, Record record) {

        File userDir = getUserDir(user);

        File camDir = getCameraDir(userDir, camera);


        return camDir.getPath() + "/rec_" +
                FILE_NAME_DATETIME_FORMATTER.format(record.getFechaInicio()) + "_" +
                FILE_NAME_DATETIME_FORMATTER.format(record.getFechaFin()) +
                ".mp4";
    }

    /**
     * Genera el path temporal para un fichero mp4 de una cámara en su directorio.
     *
     * @param user   usuario.
     * @param camera cámara.
     * @return path temporal.
     */
    public static String generateClipTempPath(User user, Camera camera) {

        File userDir = getUserDir(user);

        File camDir = getCameraDir(userDir, camera);

        return camDir.getPath() + "/temp.mp4";

    }

    /**
     * Genera el path de una cámara.
     *
     * @param userDir directorio del usuario.
     * @param camera  cámara.
     * @return directorio de la cámara.
     */
    private static File getCameraDir(File userDir, Camera camera) {

        File camDir = new File(userDir.getPath() + "/cam_" + camera.getId());

        if (!camDir.exists()) {
            camDir.mkdir();
        }

        return camDir;
    }

    /**
     * Genera el directorio de un usuario.
     *
     * @param user usuario.
     * @return directorio del usuario.
     */
    private static File getUserDir(User user) {

        File userDir = new File(BASE_PATH + "/user_" + user.getId());

        if (!userDir.exists()) {
            userDir.mkdir();
        }

        return userDir;

    }

    /**
     * Crea el directorio principal del programa.
     */
    public static void evaluateBasePath() {

        File basepath = new File(BASE_PATH);

        if (!basepath.exists()) {
            basepath.mkdir();
        }

    }


}

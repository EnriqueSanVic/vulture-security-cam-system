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
import java.util.Calendar;
import java.util.Locale;

public class ClipHandler {

    private static String BASE_PATH = "./storage";

    public static DateTimeFormatter FILE_NAME_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);


    public static Record moveTemFileToFinalPath(User user, Camera camera, Record incompleteRecord){

        Path tempPath = new File(generateClipTempPath(user, camera)).toPath();

        Path finalPath = Paths.get(generateClipFinalPath(user, camera, incompleteRecord));

        try {
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Clip guardado en: " + finalPath.toString());

        return new Record(incompleteRecord, finalPath.toString());

    }

    private static String generateClipFinalPath(User user, Camera camera, Record record){

        File userDir = getUserDir(user);

        File camDir = getCameraDir(userDir, camera);


        return  camDir.getPath().toString() + "/rec_" +
                FILE_NAME_DATETIME_FORMATTER.format(record.getFechaInicio()) + "_" +
                FILE_NAME_DATETIME_FORMATTER.format(record.getFechaFin()) +
                ".mp4";
    }

    public static String generateClipTempPath(User user, Camera camera){

        File userDir = getUserDir(user);

        File camDir = getCameraDir(userDir, camera);

        return  camDir.getPath().toString() + "/temp.mp4";

    }

    private static File getCameraDir(File userDir, Camera camera) {

        File camDir = new File(userDir.getPath().toString() + "/cam_" + camera.getId());

        if(!camDir.exists()){
            camDir.mkdir();
        }

        return camDir;
    }

    private static File getUserDir(User user){

        File userDir = new File(BASE_PATH + "/user_" + user.getId());

        if(!userDir.exists()){
            userDir.mkdir();
        }

        return userDir;

    }

    public static void evaluateBasePath(){

        File basepath = new File(BASE_PATH);

        if(!basepath.exists()){
            basepath.mkdir();
        }

    }


}

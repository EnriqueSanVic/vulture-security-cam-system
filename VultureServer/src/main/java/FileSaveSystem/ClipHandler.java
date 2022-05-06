package FileSaveSystem;

import Models.Camera;
import Models.User;

import java.io.File;
import java.io.IOException;

public class ClipHandler {

    private static String BASE_PATH = "./storage";

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

package Models;

import java.util.Date;

public class Camera {

    int id;
    int id_user;
    String name;
    Date last_transmission;
    int ref_hilo;

    public Camera(int id, int id_user, String name, Date last_transmission, int ref_hilo) {
        this.id = id;
        this.id_user = id_user;
        this.name = name;
        this.last_transmission = last_transmission;
        this.ref_hilo = ref_hilo;
    }

    public int getId() {
        return id;
    }

    public int getId_user() {
        return id_user;
    }

    public String getName() {
        return name;
    }

    public Date getLast_transmission() {
        return last_transmission;
    }

    public int getRef_hilo() {
        return ref_hilo;
    }
}

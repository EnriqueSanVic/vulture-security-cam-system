package Models;

import java.time.LocalDateTime;


public class Camera {

    private long id;
    private long id_user;
    private String name;
    private LocalDateTime last_transmission;
    private long ref_hilo;

    public Camera(long id, long id_user, String name, LocalDateTime last_transmission, long ref_hilo) {
        this.id = id;
        this.id_user = id_user;
        this.name = name;
        this.last_transmission = last_transmission;
        this.ref_hilo = ref_hilo;
    }

    public long getId() {
        return id;
    }

    public long getId_user() {
        return id_user;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLast_transmission() {
        return last_transmission;
    }

    public long getRef_hilo() {
        return ref_hilo;
    }

    @Override
    public String toString() {
        return
                id + " - " + name;
    }
}

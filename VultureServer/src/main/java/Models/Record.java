package Models;

import java.time.LocalDateTime;
import java.util.Date;

public class Record {

    private long id;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String path;
    private long id_camara;

    public Record(long id, LocalDateTime fechaInicio, LocalDateTime fechaFin, String path, long id_camara) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.path = path;
        this.id_camara = id_camara;
    }

    public Record(LocalDateTime fechaInicio, LocalDateTime fechaFin, String path, long id_camara) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.path = path;
        this.id_camara = id_camara;
    }

    public Record(Record incompletRecord, String path) {
        this.id = incompletRecord.id;
        this.fechaInicio = incompletRecord.fechaInicio;
        this.fechaFin = incompletRecord.fechaFin;
        this.path = path;
        this.id_camara = incompletRecord.id_camara;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public String getPath() {
        return path;
    }

    public long getId_camara() {
        return id_camara;
    }
}

package Models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de una petición de alto nivel de un cliente, este está preparado para ser
 * serializado y deserizalizado en Json con la libraría Gson.
 * <p>
 * Muchos campos irán nulos en muchas peticiones dado que hay peticioens que no usan todos los cambios pero en cambio se usan en otras.
 */
public class ClientRequest {

    @SerializedName("request")
    private String request;

    @SerializedName("user")
    private String user;
    @SerializedName("password")
    private String password;

    @SerializedName("cam_id")
    private long camId;

    @SerializedName("clip_id")
    private long clipId;

    public String getRequest() {
        return request;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public long getCamId() {
        return camId;
    }

    public long getClipId() {
        return clipId;
    }
}

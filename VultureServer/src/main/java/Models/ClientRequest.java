package Models;

import com.google.gson.annotations.SerializedName;

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

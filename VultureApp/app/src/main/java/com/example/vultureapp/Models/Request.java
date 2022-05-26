package com.example.vultureapp.Models;

import com.google.gson.annotations.SerializedName;

public class Request {

    public static String LOGIN_REQUEST_COMMAND = "login";
    public static String LIST_CAMS_REQUEST_COMMAND = "list_of_cams";
    public static String LIST_CLIPS_REQUEST_COMMAND = "list_of_clips";
    public static String CLIP_DOWNLOAD_REQUEST_COMMAND = "clip_download";
    public static String STREAMING_TRANSMISSION_REQUEST_COMMAND = "streaming_transmision";

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

    public Request(String request, String user, String password, long camId, long clipId) {
        this.request = request;
        this.user = user;
        this.password = password;
        this.camId = camId;
        this.clipId = clipId;
    }

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

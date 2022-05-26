package com.example.vultureapp.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Response {

    @SerializedName("status")
    private boolean status;

    @SerializedName("cam_id")
    private int camId;

    @SerializedName("list_of_clips")
    private ArrayList<ClipResponse> listOfClips;

    @SerializedName("list_of_cams")
    private ArrayList<CamResponse> listOfCams;

    @SerializedName("n_bytes_of_clip")
    private long nBytesOfClip;

    public static class ClipResponse{

        @SerializedName("id")
        protected long id;
        @SerializedName("date_time")
        protected String dateTime;

    }

    public static class CamResponse{

        @SerializedName("id")
        protected long id;
        @SerializedName("date_time")
        protected String name;

    }

    public boolean isStatus() {
        return status;
    }

    public int getCamId() {
        return camId;
    }

    public ArrayList<ClipResponse> getListOfClips() {
        return listOfClips;
    }

    public ArrayList<CamResponse> getListOfCams() {
        return listOfCams;
    }

    public long getnBytesOfClip() {
        return nBytesOfClip;
    }

    //mutaror
    public Camera[] getMutateListOfCam(){

        Camera[] camsMutate = new Camera[listOfCams.size()];

        for(int i=0; i < camsMutate.length; i++){
            camsMutate[i] = new Camera(listOfCams.get(i).id, listOfCams.get(i).name);
        }



        return camsMutate;
    }

    //mutaror
    public Clip[] getMutateListOfClips(){

        Clip[] clipsMutate = new Clip[listOfClips.size()];

        for(int i=0; i < listOfClips.size(); i++){
            clipsMutate[i] = new Clip(listOfClips.get(i).id, listOfClips.get(i).dateTime);
        }

        return clipsMutate;
    }
}

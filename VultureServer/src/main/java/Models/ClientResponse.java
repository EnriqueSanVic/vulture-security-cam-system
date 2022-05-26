package Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ClientResponse {

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

    public ClientResponse(boolean status, int camId, ArrayList<ClipResponse> listOfClips, ArrayList<CamResponse> listOfCams, long nBytesOfClip) {
        this.status = status;
        this.camId = camId;
        this.listOfClips = listOfClips;
        this.listOfCams = listOfCams;
        this.nBytesOfClip = nBytesOfClip;
    }

    public static class ClipResponse{

        @SerializedName("id")
        protected long id;
        @SerializedName("date_time")
        protected String dateTime;

        public ClipResponse(long id, String dateTime) {
            this.id = id;
            this.dateTime = dateTime;
        }

        public static ArrayList<ClipResponse> mutateList(ArrayList<Record> list) {

            ArrayList<ClipResponse> mutateList = new ArrayList<ClipResponse>(list.size());

            for(Record rec:list){
                mutateList.add(new ClipResponse(rec.getId(), transformDateTimeName(rec)));
            }

            return mutateList;
        }

        private static String transformDateTimeName(Record rec) {

            String name = "";

            name += rec.getFechaInicio().toString().replace("T", " ");
            name += " - ";
            name += rec.getFechaFin().toString().replace("T", " ");

            return name;
        }
    }

    public static class CamResponse{

        @SerializedName("id")
        protected long id;
        @SerializedName("date_time")
        protected String name;

        public CamResponse(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ArrayList<CamResponse> mutateList(ArrayList<Camera> list){

            ArrayList<CamResponse> mutateList = new ArrayList<CamResponse>(list.size());

            for(Camera cam:list){
                mutateList.add(new CamResponse(cam.getId(), cam.getName()));
            }
            return mutateList;
        }

    }

}

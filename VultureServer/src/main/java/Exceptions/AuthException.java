package Exceptions;

public class AuthException extends Exception{

    //id of user that has tryed loged
    private long userId;
    private long camID;
    private String camName;

    public AuthException(long userId, long camID,String camName) {
        this.userId = userId;
        this.camID = camID;
        this.camName = camName;
    }

    @Override
    public String getMessage() {

        if(userId < 0){
            return "AuthException: Camera not authentificated: the camera that have id: " + camID + " and name " + camName + " not autentificated, it´s possible that camera not exist in the database or not belong to user " + userId + '.';
        }else if(camID < 0){
            return "AuthException: Camera not authentificated: the user that have id: " + userId + " not autentificated, it´s possible that user not exist in the database.";
        }else{
            return "AuthException: The camera not belong to user, the references in the database not is the same.";
        }

    }
}

package Exceptions;

public class AuthException extends Exception{

    //id of user that has tryed loged
    int id;

    public AuthException(int id) {
        super("AuthException: id of user that has tryed loged: " + id);
        this.id = id;
    }
}

package Models;

public class User {

    private int id;
    private String mail;
    private String password;

    public User(int id, String mail, String password) {
        this.id = id;
        this.mail = mail;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }
}

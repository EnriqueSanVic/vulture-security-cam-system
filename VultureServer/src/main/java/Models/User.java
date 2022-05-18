package Models;

public class User {

    private long id;
    private String mail;
    private String password;

    public User(long id, String mail, String password) {
        this.id = id;
        this.mail = mail;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return  id +
                " - " + mail;
    }
}

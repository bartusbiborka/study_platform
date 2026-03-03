package JdbcExample;

public class User {
    private String id;
    private String nume;
    private String prenume;
    private String id_rol;

    public User(String id, String nume, String prenume, String id_rol) {
        this.id = id;
        this.nume = nume;
        this.prenume = prenume;
        this.id_rol = id_rol;
    }

    public String getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public String getId_rol() {
        return id_rol;
    }

    public void setId_rol(String id_rol) {
        this.id_rol = id_rol;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                '}';
    }
}

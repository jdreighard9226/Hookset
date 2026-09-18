package shared.jpa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @Column(name = "username")
    private String username;

    @Column(name = "hashPassword")
    private String hashPassword;

    @Column(name = "isAdmin")
    private Boolean isAdmin;

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}

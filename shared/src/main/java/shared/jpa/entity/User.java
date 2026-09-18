package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a user stored in the Hookset database.
 *
 * <p>Each user record stores the username, hashed password, and administrator
 * status used for authentication and authorization within Hookset.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>GeeksforGeeks - Hibernate @GeneratedValue Annotation in JPA:
 *   Utilized to find syntax needed for when mysql auto increments primary keys
 *   https://www.geeksforgeeks.org/advance-java/hibernate-generatedvalue-annotation-in-jpa/</li>
 * </ul>
 */
@Entity
@Table(name = "users")
public class User {

    /** The unique database identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    /** The username associated with the user account. */
    @Column(name = "username")
    private String username;

    /** The hashed password associated with the user account. */
    @Column(name = "hashPassword")
    private String hashPassword;

    /** Indicates whether the user has administrator privileges. */
    @Column(name = "isAdmin")
    private Boolean isAdmin;

    /**
     * Returns the unique identifier for the user.
     *
     * @return the user identifier
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Returns the username associated with the user account.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the hashed password associated with the user account.
     *
     * @return the hashed password
     */
    public String getHashPassword() {
        return hashPassword;
    }

    /**
     * Returns whether the user has administrator privileges.
     *
     * @return true if the user is an administrator, false otherwise
     */
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * Sets the username associated with the user account.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the hashed password associated with the user account.
     *
     * @param hashPassword the hashed password
     */
    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    /**
     * Sets whether the user has administrator privileges.
     *
     * @param isAdmin true if the user is an administrator, false otherwise
     */
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
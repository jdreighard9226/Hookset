package entities.jpa.entity;

import jakarta.persistence.*;

import java.util.Objects;

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
@Table(name = "Users")
public class User {

    /** The unique database identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
     * @return the user identifier, or {@code null} if the user hasn't been saved yet
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the username associated with the user account.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
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
     * Returns the hashed password associated with the user account.
     *
     * @return the hashed password
     */
    public String getHashPassword() {
        return this.hashPassword;
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
     * Returns whether the user has administrator privileges.
     *
     * @return {@code true} if the user is an administrator, otherwise {@code false}
     */
    public Boolean getIsAdmin() {
        return this.isAdmin;
    }

    /**
     * Sets whether the user has administrator privileges.
     *
     * @param isAdmin {@code true} if the user is an administrator, otherwise {@code false}
     */
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Checks whether this user is the same as another object.
     *
     * <p>Two users are equal when they have the same username, since usernames are unique.
     * The ID is not compared, so an unsaved user can still be equal to a saved one with
     * the same username. The password is left out so changing it doesn't change who the
     * user is.</p>
     *
     * @param o the object to compare against
     * @return true if the other object is a user with the same username
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(getUsername(), user.getUsername());
    }

    /**
     * Returns a hash code built from the user's username instead of its ID.
     *
     * <p>The ID can't be used here because it is null until the user is saved,
     * and a hash code that changes after saving would lose the user inside a
     * {@code HashSet}.</p>
     *
     * @return the hash code for the user
     */
    @Override
    public int hashCode() {
        return Objects.hash(getUsername());
    }

    /**
     * Returns a readable summary of the user for logging.
     *
     * <p>The password hash is left out on purpose so it never ends up in a log file.</p>
     *
     * @return the user's ID, username, and admin status
     */
    @Override
    public String toString() {
        return "User{id=" + this.id
                + ", username='" + this.username + "'"
                + ", isAdmin=" + this.isAdmin + "}";
    }
}
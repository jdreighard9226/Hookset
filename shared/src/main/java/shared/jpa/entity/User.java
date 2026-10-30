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
     * Checks whether another object is the same user record.
     *
     * <p>Two users are equal only when both have been saved and share the same
     * database ID. A user that hasn't been saved yet is only equal to itself.</p>
     *
     * @param other the object to compare against
     * @return {@code true} if both are the same saved user, otherwise {@code false}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof User otherUser)) {
            return false;
        }

        if (this.getId() == null || otherUser.getId() == null) {
            return false;
        }

        return this.getId().equals(otherUser.getId());
    }

    /**
     * Returns a hash code that stays the same for the user's whole life.
     *
     * <p>The ID can't be used here because it is null until the user is saved,
     * and a hash code that changes after saving would lose the user inside a
     * {@code HashSet}.</p>
     *
     * @return the hash code for the user
     */
    @Override
    public int hashCode() {
        return User.class.hashCode();
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
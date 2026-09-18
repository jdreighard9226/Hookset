package edu.carroll.cs341.hookset.web.form;

/**
 * Represents the data submitted through the Hookset login form.
 *
 * <p>This form object stores the username and password entered by the user.</p>
 */
public class LoginForm {

    /** The username entered into the login form. */
    private String username;

    /** The password entered into the login form. */
    private String password;

    /**
     * Returns the username entered into the login form.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username entered into the login form.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password entered into the login form.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password entered into the login form.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
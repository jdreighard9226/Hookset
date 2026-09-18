package edu.carroll.cs341.hookset.web.form;

/**
 * Represents the data submitted through the Hookset signup form.
 *
 * <p>This form object stores the username, password, and password confirmation
 * entered by a user when creating a new Hookset account.</p>
 */
public class SignupForm {

    /** The username entered into the signup form. */
    private String username;

    /** The password entered into the signup form. */
    private String password;

    /** The password confirmation entered into the signup form. */
    private String confirmPassword;

    /**
     * Returns the username entered into the signup form.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username entered into the signup form.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password entered into the signup form.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password entered into the signup form.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the password confirmation entered into the signup form.
     *
     * @return the password confirmation
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the password confirmation entered into the signup form.
     *
     * @param confirmPassword the password confirmation
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
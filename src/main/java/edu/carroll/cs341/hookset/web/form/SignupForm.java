package edu.carroll.cs341.hookset.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the data submitted through the Hookset signup form.
 *
 * <p>This form object stores the username, password, and password confirmation
 * entered by a user when creating a new Hookset account.</p>
 */
public class SignupForm {

    /** The username entered into the signup form. */
    @NotBlank(message = "Username is required.")
    @Size(min = 6, max = 20, message = "Username must be between 6 and 20 characters.")
    private String username;

    /** The password entered into the signup form. */
    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters.")
    private String password;

    /** The password confirmation entered into the signup form. */
    @NotBlank(message = "Password confirmation is required.")
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

    /**
     * Checks whether the password and password confirmation match.
     *
     * <p>This method is used by bean validation through {@link AssertTrue}.
     * Validation reports the result under the passwordMatching property.</p>
     *
     * @return true if the passwords match or either one is missing, false otherwise
     */
    @AssertTrue(message = "Passwords do not match.")
    public boolean isPasswordMatching() {

        // missing fields are already reported by @NotBlank,
        // so skip the match check to avoid a second error
        if (password == null || confirmPassword == null) {
            return true;
        }

        return password.equals(confirmPassword);
    }
}
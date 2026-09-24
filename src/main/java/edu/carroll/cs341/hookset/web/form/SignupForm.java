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
    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String password;

    /** The password confirmation entered into the signup form. */
    @NotBlank(message = "Password confirmation is required.")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @AssertTrue(message = "Passwords do not match.")
    public boolean isPasswordMatching() {
        if (password == null || confirmPassword == null) {
            return true;
        }

        return password.equals(confirmPassword);
    }
}
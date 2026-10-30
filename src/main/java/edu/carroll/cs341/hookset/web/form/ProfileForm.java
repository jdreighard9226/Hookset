package edu.carroll.cs341.hookset.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Holds the data from the Hookset profile form.
 *
 * <p>The username is filled in by the controller so the page can show it, and
 * is never changed through this form. The password and confirmation are
 * checked against their annotations before the password change reaches the
 * user service.</p>
 */
public class ProfileForm {

    /** The logged-in user's username, shown on the profile page. */
    private String username;

    /** The new password entered into the profile form. */
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 18, message = "Password must be between 8 and 18 characters.")
    private String password;

    /** The new password confirmation entered into the profile form. */
    @NotBlank(message = "Password confirmation is required.")
    private String confirmPassword;

    /**
     * Returns the logged-in user's username.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the logged-in user's username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the new password entered into the profile form.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the new password entered into the profile form.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the new password confirmation entered into the profile form.
     *
     * @return the password confirmation
     */
    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    /**
     * Sets the new password confirmation entered into the profile form.
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
     * @return {@code true} if the passwords match or either one is missing, otherwise {@code false}
     */
    @AssertTrue(message = "Passwords do not match.")
    public boolean isPasswordMatching() {
        // missing fields are already reported by @NotBlank,
        // so skip the match check to avoid a second error
        if (this.password == null || this.confirmPassword == null) {
            return true;
        }

        return this.password.equals(this.confirmPassword);
    }
}
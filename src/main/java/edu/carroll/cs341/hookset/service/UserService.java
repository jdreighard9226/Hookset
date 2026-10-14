package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.ProfileForm;
import edu.carroll.cs341.hookset.web.form.SignupForm;

public interface UserService {
    /**
     * Changes the password for the logged-in user.
     *
     * @param profileForm the form containing the new password and its confirmation
     * @return {@code true} if the password was changed, otherwise {@code false}
     */
    boolean changePassword(ProfileForm profileForm);

    /**
     * Creates a new user account from the submitted signup form.
     *
     * @param signupForm the form containing the username and password
     * @return true if the user was created, false if the username is taken
     */
    boolean addUser(SignupForm signupForm);

    boolean isUsernameBlank(String username);

    boolean isUsernameTooLong(String username);

    boolean isUsernameSpaceFree(String username);

    boolean isUsernameTaken(String username);


    boolean doPasswordsMatch(ProfileForm profileForm);

    boolean isPasswordBlank(String password);

    boolean isPasswordTooShort(String password);

    boolean isPasswordSpaceFree(String password);
}

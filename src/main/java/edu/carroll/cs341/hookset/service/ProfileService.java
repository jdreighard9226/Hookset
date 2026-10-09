package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.ProfileForm;

/**
 * Service for managing the logged-in user's profile.
 *
 * <p>This interface defines the profile operations used by the web layer.
 * It currently only supports changing the logged-in user's password.</p>
 */
public interface ProfileService {

    /**
     * Changes the password for the logged-in user.
     *
     * @param profileForm the form containing the new password and its confirmation
     * @return {@code true} if the password was changed, otherwise {@code false}
     */
    boolean changePassword(ProfileForm profileForm);
}
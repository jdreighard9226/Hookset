package edu.carroll.cs341.hookset.service.input;

/**
 * The details a caller provides when changing a user's password.
 *
 * <p>This is the user service's input, separate from the profile form, so the
 * service doesn't depend on where the data came from. The user is never part
 * of it, so the service always works on the user whose ID it's given.</p>
 *
 * @param password the new password
 * @param confirmPassword the new password typed a second time
 */
public record PasswordChangeInput(String password,
                                  String confirmPassword) {
}
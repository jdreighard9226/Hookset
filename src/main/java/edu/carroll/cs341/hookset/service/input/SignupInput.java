package edu.carroll.cs341.hookset.service.input;

/**
 * The details a caller provides when creating a new user account.
 *
 * <p>This is the user service's input, separate from the signup form, so the
 * service doesn't depend on where the data came from. It only holds what the
 * person signing up is allowed to choose. The user ID and admin status are
 * never part of it, so the service always decides those itself.</p>
 *
 * @param username the username for the new account
 * @param password the password for the new account
 * @param confirmPassword the password typed a second time
 */
public record SignupInput(String username,
                          String password,
                          String confirmPassword) {
}
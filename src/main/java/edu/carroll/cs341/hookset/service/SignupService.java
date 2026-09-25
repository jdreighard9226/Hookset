package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.SignupForm;

/**
 * Defines the operations used to create new Hookset user accounts.
 */
public interface SignupService {

    /**
     * Creates a new user account from the submitted signup form.
     *
     * @param signupForm the form containing the username and password
     * @return true if the user was created, false if the username is taken
     */
    boolean addUser(SignupForm signupForm);
}
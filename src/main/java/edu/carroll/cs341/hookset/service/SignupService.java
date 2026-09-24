package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.SignupForm;

public interface SignupService {
    boolean addUser(SignupForm signupForm);
}

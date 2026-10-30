package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.service.input.PasswordChangeInput;
import edu.carroll.cs341.hookset.service.input.SignupInput;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.springframework.stereotype.Component;

/**
 * Maps user forms to user service input.
 *
 * <p>The signup and profile forms are web classes, so they are converted here
 * before reaching the user service. That way the service never depends on the
 * web layer.</p>
 */
@Component
public class UserMapper {

    /**
     * Converts a submitted signup form into service input.
     *
     * @param signupForm the submitted signup form
     * @return the signup input built from the form
     */
    public SignupInput toSignupInput(SignupForm signupForm) {
        return new SignupInput(
                signupForm.getUsername(),
                signupForm.getPassword(),
                signupForm.getConfirmPassword()
        );
    }

    /**
     * Converts a submitted profile form into password change input.
     *
     * @param profileForm the submitted profile form
     * @return the password change input built from the form
     */
    public PasswordChangeInput toPasswordChangeInput(ProfileForm profileForm) {
        return new PasswordChangeInput(
                profileForm.getPassword(),
                profileForm.getConfirmPassword()
        );
    }
}
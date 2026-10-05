package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.ProfileForm;
import org.springframework.transaction.annotation.Transactional;

public interface ProfileService {
    @Transactional
    boolean changePassword(ProfileForm profileForm);
}

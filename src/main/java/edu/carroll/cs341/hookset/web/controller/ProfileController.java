package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.ProfileService;
import edu.carroll.cs341.hookset.web.form.LoginForm;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {

        model.addAttribute("profileForm", new ProfileForm());
        return "/profile";
    }
}

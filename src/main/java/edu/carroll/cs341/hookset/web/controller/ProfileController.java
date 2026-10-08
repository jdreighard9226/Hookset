package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.ProfileService;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {
    private final ProfileService profileService;
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String getProfile(Model model, Authentication authentication) {
        ProfileForm profileForm = new ProfileForm();

        profileForm.setUsername(authentication.getName());

        model.addAttribute("profileForm", profileForm);

        return "profile";
    }

    @PostMapping("/profile")
    public String postProfile(@Valid @ModelAttribute ProfileForm profileForm, BindingResult result) {
        if (result.hasErrors()) {
            log.warn("Profile form failed validation with {} error(s)", result.getErrorCount());
            return "profile";
        }

        boolean changePassword = profileService.changePassword(profileForm);

        if (!changePassword) {
            result.reject("password.change.failed",
                    "Unable to change password. Please try again.");
            return "profile";
        }

        return "redirect:/";
    }
}

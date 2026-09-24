package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.SignupService;
import edu.carroll.cs341.hookset.service.impl.SignupServiceImpl;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SignupController {
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String signupGet(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String loginPost(@Valid @ModelAttribute SignupForm signupForm, BindingResult result, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "signup";
        }

        if (signupService.addUser(signupForm)) {

            attrs.addFlashAttribute(
                    "signupSuccess",
                    "Account created successfully. You can now log in."
            );

            return "redirect:/login";
        }

        result.reject(
                "usernameExists",
                "That username is already taken."
        );

        return "signup";

    }
}

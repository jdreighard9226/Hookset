package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    @GetMapping("/signup")
    public String signupGet(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupPost() {
        return "redirect:/login";
    }
}

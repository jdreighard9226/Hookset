package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.SignupService;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

@Service
public class SignupServiceImpl implements SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean addUser(SignupForm signupForm) {
        User user = userRepository.findByUsername(signupForm.getUsername());
        if (user != null) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(signupForm.getUsername());
        newUser.setIsAdmin(false);
        newUser.setHashPassword(
                passwordEncoder.encode(signupForm.getPassword())
        );

        userRepository.save(newUser);
        return true;
    }

}

package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.SignupRepository;
import edu.carroll.cs341.hookset.service.SignupService;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

@Service
public class SignupServiceImpl implements SignupService {
    private final SignupRepository signupRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupServiceImpl(SignupRepository signupRepository, PasswordEncoder passwordEncoder) {
        this.signupRepository = signupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean addUser(SignupForm signupForm) {
        User user = signupRepository.findByUsername(signupForm.getUsername());
        if (user != null) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(signupForm.getUsername());
        newUser.setIsAdmin(false);
        newUser.setHashPassword(
                passwordEncoder.encode(signupForm.getPassword())
        );

        signupRepository.save(newUser);
        return true;
    }

}

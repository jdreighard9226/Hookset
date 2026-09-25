package edu.carroll.cs341.hookset.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import shared.jpa.entity.User;

import java.util.Collection;
import java.util.List;

public class HooksetUserDetails implements UserDetails {

    private final User user;

    public HooksetUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getHashPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    public Long getUserId() {
        return user.getUserId();
    }
}
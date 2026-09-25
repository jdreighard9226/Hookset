package edu.carroll.cs341.hookset.service;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Defines the service Spring Security uses to load Hookset users during login.
 *
 * <p>This interface extends {@link UserDetailsService} so the implementation
 * can be used directly by Spring Security's form login.</p>
 */
public interface LoginService extends UserDetailsService {
}
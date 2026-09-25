package edu.carroll.cs341.hookset.userDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import shared.jpa.entity.User;

import java.util.Collection;
import java.util.List;

/**
 * Wraps a Hookset {@link User} so it can be used by Spring Security.
 *
 * <p>Spring Security works with {@link UserDetails} objects during login.
 * This class passes the username and hashed password from the database user
 * to Spring Security and also exposes the Hookset user id.</p>
 */
public class HooksetUserDetails implements UserDetails {

    /** The database user this object wraps. */
    private final User user;

    /**
     * Creates the user details from a database user.
     *
     * @param user the database user to wrap
     */
    public HooksetUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the username used to log in.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Returns the hashed password Spring Security checks during login.
     *
     * @return the hashed password
     */
    @Override
    public String getPassword() {
        return user.getHashPassword();
    }

    /**
     * Returns the roles granted to the user.
     *
     * @return the user's authorities, currently always empty
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // roles are not set up yet. the isAdmin flag will be used for ADMIN / USER
        return List.of();
    }

    /**
     * Returns the database identifier of the user.
     *
     * @return the user identifier
     */
    public Long getUserId() {
        return user.getUserId();
    }
}
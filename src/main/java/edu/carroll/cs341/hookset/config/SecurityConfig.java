package edu.carroll.cs341.hookset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures authentication and authorization rules for the Hookset web application.
 *
 * <p>This configuration determines which pages can be accessed without authentication
 * and requires users to authenticate before accessing all other pages. It also
 * configures Hookset to use a custom login page.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Spring Framework - Java-based Container Configuration:
 *   https://docs.spring.io/spring-framework/reference/core/beans/java.html</li>
 *   <li>Spring Security - Authorize HTTP Requests:
 *   https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html</li>
 *   <li>Spring Security - Form Login:
 *   https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * Creates the password encoder used to hash user passwords.
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates the security filter chain used to control access to Hookset pages
     * and configure form-based authentication.
     *
     * <p>The home, login, and signup pages are publicly accessible. All other
     * requests require the user to be authenticated. The login form is served
     * from the /login page and authentication requests are processed through
     * the /login URL.</p>
     *
     * @param http the HttpSecurity object used to configure web security
     * @return the configured security filter chain
     * @throws Exception if an error occurs while building the security configuration
     *
     * Modified from Spring SecurityFilterSecurity example on page:
     * https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html
     */
    @Bean
    public SecurityFilterChain securedFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/css/**",
                                "/images/**",
                                "/js/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .permitAll()
                );

        return http.build();
    }
}
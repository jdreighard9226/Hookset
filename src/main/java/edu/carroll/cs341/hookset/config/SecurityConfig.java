package edu.carroll.cs341.hookset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures authentication and authorization rules for the Hookset web application.
 *
 * <p>This configuration determines which pages can be accessed without authentication
 * and requires users to authenticate before accessing all other pages. It also
 * configures Hookset to use a custom login page and manages user sessions
 * after login.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Spring Framework - Java-based Container Configuration:
 *   https://docs.spring.io/spring-framework/reference/core/beans/java.html</li>
 *   <li>Spring Security - Authorize HTTP Requests:
 *   https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html</li>
 *   <li>Spring Security - Form Login:
 *   https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html</li>
 *   <li>Spring Security - Session Management:
 *   https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html</li>
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
     * <p>A session is created after a successful login so the user stays signed
     * in between requests. The session id is changed on login to protect against
     * session fixation attacks.</p>
     *
     * <p>Modified from the Spring Security form login example on page:
     * https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html</p>
     *
     * @param http the HttpSecurity object used to configure web security
     * @return the configured security filter chain
     * @throws Exception if an error occurs while building the security configuration
     */
    @Bean
    public SecurityFilterChain securedFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // pages and static files anyone can visit
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/css/**",
                                "/images/**",
                                "/js/**"
                        ).permitAll()

                        // everything else requires a logged in user
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        // use the custom login page instead of the Spring default
                        .loginPage("/login")
                        // Spring Security handles the POST to /login
                        .loginProcessingUrl("/login")
                        // always send the user home after login
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .sessionManagement(session -> session
                        // only create a session when one is needed, such as after login
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // give the user a new session id on login to prevent session fixation
                        .sessionFixation(sessionFixation ->
                                sessionFixation.changeSessionId()
                        )
                );

        return http.build();
    }
}
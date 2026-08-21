package com.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.bank.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * Password encoder used when registering users
     * and authenticating login credentials.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Authentication provider.
     *
     * UserService loads the user from the database.
     * BCrypt verifies the submitted password.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserService userService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    /*
     * Main Spring Security configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                /*
                 * Public pages and resources.
                 */
                .requestMatchers(
                    "/login",
                    "/register",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                /*
                 * Admin pages require ADMIN role.
                 */
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                /*
                 * Everything else requires authentication.
                 */
                .anyRequest()
                .authenticated()
            )

            /*
             * Login configuration.
             */
            .formLogin(form -> form

                /*
                 * Custom login page.
                 */
                .loginPage("/login")

                /*
                 * Spring Security processes POST /login.
                 */
                .loginProcessingUrl("/login")

                /*
                 * Redirect after successful login.
                 */
                .defaultSuccessUrl(
                    "/dashboard",
                    true
                )

                /*
                 * Redirect after failed login.
                 */
                .failureUrl("/login?error")

                /*
                 * Allow everyone to access the login page.
                 */
                .permitAll()
            )

            /*
             * Logout configuration.
             */
            .logout(logout -> logout

                /*
                 * Logout endpoint.
                 */
                .logoutUrl("/logout")

                /*
                 * IMPORTANT:
                 * Redirect directly to /login.
                 * No ?logout parameter.
                 */
                .logoutSuccessUrl("/login")

                /*
                 * Invalidate the current session.
                 */
                .invalidateHttpSession(true)

                /*
                 * Clear the current authentication.
                 */
                .clearAuthentication(true)

                /*
                 * Allow logout.
                 */
                .permitAll()
            );

        /*
         * CSRF protection remains enabled.
         * Thymeleaf POST forms should include the CSRF token.
         */
        return http.build();
    }
}
package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/campagnes/**", "/employes/**",
                                "/feuilles/**", "/visites/**")
                        .hasRole("GESTIONNAIRE")

                        .requestMatchers(
                                "/home", "/campagnes/**", "/employes/**",
                                "/feuilles/**", "/visites/**")
                        .hasAnyRole("GESTIONNAIRE", "CONSULTANT")

                        .anyRequest().authenticated()
                // ↑ Toutes les URLs sont accessibles sans être connecté

                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((req, res, auth) -> {
                            String role = auth.getAuthorities().iterator().next().getAuthority();
                            if ("ROLE_ADMIN".equals(role))
                                res.sendRedirect("/admin/utilisateurs");
                            else if ("ROLE_CONSULTANT".equals(role))
                                res.sendRedirect("/home");
                            else
                                res.sendRedirect("/home");
                        })
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
               ;
        return http.build();
    }
}
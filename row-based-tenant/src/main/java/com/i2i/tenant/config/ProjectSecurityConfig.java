package com.i2i.tenant.config;

import com.i2i.tenant.exceptionalhandling.CustomAccessDeniedHandler;
import com.i2i.tenant.exceptionalhandling.CustomBasicAuthenticationEntryPoint;
import com.i2i.tenant.filter.*;
import com.i2i.tenant.repository.OrganizationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import lombok.RequiredArgsConstructor;

@Configuration
@Profile("!prod")
@RequiredArgsConstructor
public class ProjectSecurityConfig {

    private final OrganizationRepository organizationRepository;
    private final JWTTokenValidatorFilter jwtTokenValidatorFilter;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No session, only JWT
                .csrf(AbstractHttpConfigurer::disable) // CSRF disabled for APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()  // ✅ Ensure Actuator endpoints are public
                        .requestMatchers("/api/users/register", "/api/users/login", "/tenants/**").permitAll()
                        .requestMatchers("/user", "/api/users/me").authenticated()
                        .requestMatchers("/myAccount").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint())) // Optional
                .exceptionHandling(ex -> ex.accessDeniedHandler(new CustomAccessDeniedHandler())) // Custom access denied handler
                .addFilterBefore(new TenantFilter(organizationRepository), UsernamePasswordAuthenticationFilter.class) // Add TenantFilter
                .addFilterBefore(jwtTokenValidatorFilter, UsernamePasswordAuthenticationFilter.class) // JWT filter before username-password authentication
                .addFilterAfter(new AuthoritiesLoggingAfterFilter(), UsernamePasswordAuthenticationFilter.class) // Logging filter
                .cors(Customizer.withDefaults()); // Optional: Add CORS support if needed

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * From Spring Security 6.3 version
     *
     * @return
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        ProjectUsernamePwdAuthenticationProvider authenticationProvider =
                new ProjectUsernamePwdAuthenticationProvider(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return  providerManager;
    }

}

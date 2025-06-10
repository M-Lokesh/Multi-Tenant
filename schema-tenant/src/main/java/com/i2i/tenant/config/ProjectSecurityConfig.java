package com.i2i.tenant.config;

import com.i2i.tenant.exceptionalhandling.CustomAccessDeniedHandler;
import com.i2i.tenant.exceptionalhandling.CustomBasicAuthenticationEntryPoint;
import com.i2i.tenant.filter.*;
import com.i2i.tenant.service.TenantSchemaService;
import org.hibernate.cfg.Environment;
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

@Configuration
@Profile("!prod")
public class ProjectSecurityConfig {

    private final TenantFilter tenantFilter;
    private final JWTTokenValidatorFilter jwtTokenValidatorFilter;
    private final AuthoritiesLoggingAfterFilter authoritiesLoggingAfterFilter;

    public ProjectSecurityConfig(
            TenantFilter tenantFilter,
                                 JWTTokenValidatorFilter jwtTokenValidatorFilter,
                                 AuthoritiesLoggingAfterFilter authoritiesLoggingAfterFilter) {
        this.tenantFilter = tenantFilter;
        this.jwtTokenValidatorFilter = jwtTokenValidatorFilter;
        this.authoritiesLoggingAfterFilter = authoritiesLoggingAfterFilter;
    }

//    @Bean
//    public JWTTokenValidatorFilter jwtTokenValidatorFilter() {
//        return new JWTTokenValidatorFilter();
//    }
//
//    @Bean
//    public TenantFilter tenantFilter(TenantSchemaService tenantSchemaService) {
//        return new TenantFilter(tenantSchemaService);
//    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http
           // , JWTTokenValidatorFilter jwtTokenValidatorFilter,
             //                                      TenantFilter tenantFilter
    ) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No session, only JWT
                .csrf(AbstractHttpConfigurer::disable) // CSRF disabled for APIs
                .authorizeHttpRequests(auth -> auth
                       // .requestMatchers("/products").permitAll()
                        .requestMatchers("/actuator/**").permitAll()  //Actuator endpoints are public
                        .requestMatchers("/register", "/apiLogin", "/tenants/**").permitAll()
                        .requestMatchers("/user", "/loggedInUser").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()))
                .exceptionHandling(ex -> ex.accessDeniedHandler(new CustomAccessDeniedHandler()))
                // run tenant filter first to validate tenant header
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                 //then run JWT token validation
                .addFilterBefore(jwtTokenValidatorFilter, UsernamePasswordAuthenticationFilter.class)
                 //other filters
                .addFilterAfter(authoritiesLoggingAfterFilter, UsernamePasswordAuthenticationFilter.class)

                .cors(Customizer.withDefaults());
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
        return providerManager;
    }

}

package com.nexturn.ccms.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nexturn.ccms.repository.UserLoginRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    UserDetailsService userDetailsService(UserLoginRepository userLoginRepository) {
        return username -> userLoginRepository.findByEmail(username)
                .map(account -> User.withUsername(account.getEmail())
                        .password(account.getPassword())
                        .authorities("ROLE_" + account.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Account was not found"));
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}")
            String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept",
                        "X-XSRF-TOKEN", "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository contextRepository,
            CsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .securityContext(context -> context
                        .securityContextRepository(contextRepository))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":401,\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":403,\"message\":\"Access denied\"}");
                        }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/customers")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/customers/me")
                            .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/me")
                            .hasRole("CUSTOMER")
                        .requestMatchers("/api/customers/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                        .requestMatchers(HttpMethod.POST, "/api/card-applications")
                            .hasRole("CUSTOMER")
                        .requestMatchers("/api/card-applications/customer/**")
                            .hasRole("CUSTOMER")
                        .requestMatchers("/api/card-applications/**")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/card-types")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE", "CUSTOMER")
                        .requestMatchers("/api/card-types/**")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cards/customer/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/cards/*/cvv")
                            .hasRole("CUSTOMER")
                        .requestMatchers("/api/cards/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/transactions/customer/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE", "CUSTOMER")
                        .requestMatchers("/api/transactions/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/payments/customer/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE", "CUSTOMER")
                        .requestMatchers("/api/payments/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                        .requestMatchers("/api/**")
                            .hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                        .anyRequest().permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN"));

        return http.build();
    }
}

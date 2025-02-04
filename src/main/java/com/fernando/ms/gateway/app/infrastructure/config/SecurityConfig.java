package com.fernando.ms.gateway.app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/authorized").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/auth").permitAll()
                        .pathMatchers(HttpMethod.GET,"/api/v1/users/**", "/api/v1/posts/**", "/api/v1/comments/**","/api/v1/notifications/**").hasAnyAuthority("SCOPE_read")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users", "/api/v1/posts", "/api/v1/comments", "/api/likes", "/api/followers","/api/v1/notifications").hasAnyAuthority("SCOPE_read", "SCOPE_write")
                        .pathMatchers(HttpMethod.POST, "/api/v1/notifications/all").hasAnyAuthority("SCOPE_read", "SCOPE_write")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/{id}", "/api/v1/posts/{id}", "/api/v1/comments/{id}","/api/v1/notifications/{id}/read/{value}").hasAnyAuthority("SCOPE_read", "SCOPE_write")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/{id}", "/api/v1/posts/{id}", "/api/v1/comments/{id}").hasAuthority("SCOPE_write")
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2Login(login->login.loginPage("/oauth2/authorization/gateway-service"))
                .oauth2Client(withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));

        return http.build();
    }

}


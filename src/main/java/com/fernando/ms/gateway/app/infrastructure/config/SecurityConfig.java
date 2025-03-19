package com.fernando.ms.gateway.app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,ReactiveClientRegistrationRepository clientRegistrationRepository) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/authorized").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/auth","/api/v1/users").permitAll()
                        .pathMatchers(HttpMethod.GET,"/api/v1/users/**", "/api/v1/posts/**", "/api/v1/comments/**","/api/v1/notifications/**").hasAnyAuthority("SCOPE_read")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/admin", "/api/v1/posts", "/api/v1/comments", "/api/likes", "/api/followers","/api/v1/notifications").hasAnyAuthority("SCOPE_read", "SCOPE_write")
                        .pathMatchers(HttpMethod.POST, "/api/v1/notifications/all").hasAnyAuthority("SCOPE_read", "SCOPE_write")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/{id}", "/api/v1/posts/{id}", "/api/v1/comments/{id}","/api/v1/notifications/{id}/read/{value}").hasAnyAuthority("SCOPE_read", "SCOPE_write")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/{id}", "/api/v1/posts/{id}", "/api/v1/comments/{id}").hasAuthority("SCOPE_write")
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .oauth2Login(login->login.loginPage("/oauth2/authorization/gateway-service"))
//                .oauth2Client(withDefaults())
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            // Evita bloqueo con una respuesta reactiva
                            return webFilterExchange.getExchange().getResponse().setComplete();
                        })
                )
                //.oauth2Client(withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
            ;

        return http.build();
    }

    @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
        return oidcLogoutSuccessHandler;
    }
}


package com.fernando.ms.gateway.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    private final ReactiveClientRegistrationRepository repository;
    private static final String ROLE_USER="ROLE_USER";
    private static final String ROLE_ADMIN="ROLE_ADMIN";
    private static final String OIDC_USER="OIDC_USER";

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http){
        return http
                .cors(Customizer.withDefaults())
                .authorizeExchange(auth ->auth
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/api/v1/posts/me",
                                "/api/v1/posts/{id}",
                                "/api/v1/posts/{id}/verify",
                                "/api/v1/posts/count",
                                "/api/v1/posts/data/count/{postId}",
                                "/api/v1/posts/data/{postId}/post/{userId}/exists",
                                "/api/v1/posts/count",
                                "/api/v1/users/{id}/verify",
                                "/api/v1/users/find-by-ids",
                                "/api/v1/users/me",
                                "/api/v1/users/{userId}/followed",
                                "/api/v1/comments/{id}",
                                "/api/v1/comments/{id}/verify"
                        ).hasAnyAuthority(ROLE_USER,OIDC_USER)
                        .pathMatchers(HttpMethod.GET,
                                "/api/v1/posts",
                                "/api/v1/users",
                                "/api/v1/users/{id}",
                                "/api/v1/comments"
                        ).hasAnyAuthority(ROLE_ADMIN)
                        .pathMatchers(HttpMethod.POST,
                                "/api/v1/posts/**",
                                "/api/v1/comments/**",
                                "/api/v1/users/follow"
                        ).hasAnyAuthority(ROLE_USER,OIDC_USER)
                        .pathMatchers(HttpMethod.POST,  "/api/v1/users").hasAnyAuthority(ROLE_ADMIN)

                        .pathMatchers(HttpMethod.PUT,
                                "/api/v1/posts/{id}",
                                "/api/v1/comments/{id}",
                                "/api/v1/users/me"
                        ).hasAnyAuthority(ROLE_USER,OIDC_USER)
                        .pathMatchers(HttpMethod.PUT,  "/api/v1/users/{id}").hasAnyAuthority(ROLE_ADMIN)
                        .pathMatchers(HttpMethod.DELETE,
                                "/api/v1/posts/{id}",
                                "/api/v1/comments/{id}",
                                "/api/v1/posts/data/{id}",
                                "/api/v1/users/unfollow/{id}"
                        ).hasAnyAuthority(ROLE_USER,OIDC_USER)
                        .pathMatchers(HttpMethod.DELETE,  "/api/v1/users/{id}").hasAnyAuthority(ROLE_ADMIN)
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(ReactiveJwtDecoders.fromIssuerLocation(issuerUri))
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .logout(logoutSpec -> logoutSpec.logoutUrl("/logout").logoutSuccessHandler(successHandler(repository)))
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    ServerLogoutSuccessHandler successHandler(ReactiveClientRegistrationRepository repository){
        OidcClientInitiatedServerLogoutSuccessHandler successHandler=new OidcClientInitiatedServerLogoutSuccessHandler(repository);
        successHandler.setPostLogoutRedirectUri("{baseUrl}/logged-out");
        return successHandler;
    }

}


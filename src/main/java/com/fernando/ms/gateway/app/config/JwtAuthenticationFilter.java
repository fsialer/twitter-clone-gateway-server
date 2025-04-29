package com.fernando.ms.gateway.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Value("${auth-service.url}")
    private String authService;
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Usar ObjectMapper

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String token = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .map(authHeader -> authHeader.replace("Bearer ", ""))
                .orElse(null);

        if (token == null) {
            return chain.filter(exchange);
        }

        return getPublicKeyFromJwks(getKidFromToken(token))
                .flatMap(publicKey -> {
                    try {
                        // Parsear el token JWT
                        JWT jwt = JWTParser.parse(token);
                        if (!(jwt instanceof SignedJWT)) {
                            return Mono.error(new BadCredentialsException("Token no firmado"));
                        }

                        SignedJWT signedJWT = (SignedJWT) jwt;
                        // Verificar la firma del token usando la clave pública
                        RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
                        if (!signedJWT.verify(verifier)) {
                            return Mono.error(new BadCredentialsException("Token inválido"));
                        }

                        // Obtener los claims
                        String userId = signedJWT.getJWTClaimsSet().getStringClaim("user_id");

                        // Modificar la solicitud agregando el usuario
                        ServerHttpRequest modifiedRequest = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", userId)
                                .build();

                        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
                        return chain.filter(modifiedExchange);
                    } catch (ExpiredJwtException e) {
                        return Mono.error(new BadCredentialsException("Token expired: " + e.getMessage()));
                    } catch (JwtException e) {
                        return Mono.error(new BadCredentialsException("Error de JWT: " + e.getMessage()));
                    } catch (Exception e) {
                        return Mono.error(new BadCredentialsException("Error process token: " + e.getMessage()));
                    }
                });
    }

    private Mono<RSAPublicKey> getPublicKeyFromJwks(String kid) {
        return webClient.get()
                .uri(authService.concat("/oauth2/jwks"))
                .retrieve()
                .bodyToMono(Map.class)
                .map(jwks -> {
                    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
                    Map<String, Object> keyData = keys.stream()
                            .filter(k -> Objects.equals(k.get("kid"), kid))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Clave no encontrada en JWKS para kid: " + kid));
                    String n = (String) keyData.get("n");
                    String e = (String) keyData.get("e");
                    byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
                    byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

                    KeySpec keySpec = new RSAPublicKeySpec(new java.math.BigInteger(1, modulusBytes),
                            new java.math.BigInteger(1, exponentBytes));
                    try {
                        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
                    } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    public String getKidFromToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length < 2) {
                throw new IllegalArgumentException("El token no tiene el formato correcto.");
            }
            // Decodificar el header del token (Base64 URL Safe)
            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));

            // Convertir a un Map usando Jackson
            Map<String, Object> headerMap = objectMapper.readValue(headerJson, new TypeReference<Map<String, Object>>() {});

            // Obtener el kid
            String kid = (String) headerMap.get("kid");

            if (kid == null) {
                throw new IllegalArgumentException("El token no tiene un KID en su header.");
            }
            return kid;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al obtener el KID del token: " + e.getMessage(), e);
        }
    }
}

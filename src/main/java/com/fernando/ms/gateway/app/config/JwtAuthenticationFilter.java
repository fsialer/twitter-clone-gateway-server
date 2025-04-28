package com.fernando.ms.gateway.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
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
                        JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();
                        Claims claims = jwtParser.parseClaimsJws(token).getBody();
                        String userId = claims.get("user_id", String.class);
                        ServerHttpRequest modifiedRequest = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", String.valueOf(userId))
                                .build();
                        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
                        return chain.filter(modifiedExchange);
                    } catch (Exception e) {
                        return Mono.error(new BadCredentialsException("Error al procesar el token: " + e.getMessage()));
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

            // Convertir a un Map para extraer "kid"
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> headerMap = objectMapper.readValue(headerJson, Map.class);

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

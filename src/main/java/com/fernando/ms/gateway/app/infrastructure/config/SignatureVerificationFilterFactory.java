package com.fernando.ms.gateway.app.infrastructure.config;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class SignatureVerificationFilterFactory extends AbstractGatewayFilterFactory<SignatureVerificationFilterFactory.Config> {
    private static final String SIGNATURE_HEADER = "X-Signature";
    @Value("${key-secret}")
    private String SECRET_KEY;

    public SignatureVerificationFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String signature = exchange.getRequest().getHeaders().getFirst(SIGNATURE_HEADER);
                String method = exchange.getRequest().getMethod().toString();
                if(exchange.getRequest().getPath().toString().equals("/v1/users/auth")||exchange.getRequest().getPath().toString().equals("/authorized")){
                    return chain.filter(exchange);
                }

                if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
                        return chain.filter(exchange);
                }

                if (signature == null) {
                    return unauthorizedResponse(exchange);
                }


                return exchange.getRequest().getBody()
                        .collectList()
                        .flatMap(bodyList -> {
                            String body = bodyList.stream()
                                    .map(buffer -> buffer.toString(StandardCharsets.UTF_8))
                                    .collect(Collectors.joining());

                            // Volver a crear el flujo de datos para que esté disponible para el siguiente filtro
                            DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
                            DataBuffer newDataBuffer = dataBufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8));
                            Flux<DataBuffer> bodyFlux = Flux.just(newDataBuffer);

                            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return bodyFlux;  // Devolver el cuerpo modificado
                                }
                            };
                            try {
                                String xSignature = generateXSignature(body);
                                // Comparar la firma calculada con la firma recibida
                                if (!signature.equals(xSignature)) {
                                    return unauthorizedResponse(exchange);
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                return unauthorizedResponse(exchange);
                            }
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        });

            }
        };
    }

    public static class Config {
        // Configuración del filtro si es necesario
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        return Mono.defer(() -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        });
    }

//    private String generateSignature(String data) throws Exception {
//        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
//        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//        sha256Hmac.init(secretKey);
//        byte[] signatureBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//        return Base64.getEncoder().encodeToString(signatureBytes);
//    }

    private String generateXSignature(String body) {
        // Aquí debes implementar la lógica de generación de la firma X-Signature
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la firma X-Signature", e);
        }
    }
}

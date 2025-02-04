package com.fernando.ms.gateway.app.infrastructure.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCatalog {

    USER_FALLBACK("GATEWAY_MS_001","User service is not available at this time. Try later"),
    POST_FALLBACK("GATEWAY_MS_002","Post service is not available at this time. Try later"),
    COMMENT_FALLBACK("GATEWAY_MS_003","Comment service is not available at this time. Try later"),
    LIKE_FALLBACK("GATEWAY_MS_004","Like service is not available at this time. Try later"),
    FOLLOWER_FALLBACK("GATEWAY_MS_005","Follower service is not available at this time. Try later"),
    UN_AUTHORIZATION("GATEWAY_MS_006","Unauthorization."),
    GATEWAY_INTERNAL_SERVER_ERROR("GATEWAY_MS_000", "Internal server error.");
    private final String code;
    private final String message;
}

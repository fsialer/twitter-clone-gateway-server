package com.fernando.ms.gateway.app.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCatalog {

    ERROR_MSG("GATEWAY_MS_001","An occurred error."),
    GATEWAY_INTERNAL_SERVER_ERROR("GATEWAY_MS_000", "Internal server error.");
    private final String code;
    private final String message;
}

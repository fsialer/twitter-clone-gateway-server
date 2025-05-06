package com.fernando.ms.gateway.app.controllers;

import com.fernando.ms.gateway.app.dto.ErrorResponse;
import com.fernando.ms.gateway.app.exceptions.ErrorException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;

import static com.fernando.ms.gateway.app.enums.ErrorType.SYSTEM;
import static com.fernando.ms.gateway.app.utils.ErrorCatalog.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ErrorException.class)
    public Mono<ErrorResponse> handleException(ErrorException e) {

        return Mono.just(
                ErrorResponse.builder()
                        .code(ERROR_MSG.getCode())
                        .type(SYSTEM)
                        .message(ERROR_MSG.getMessage())
                        .details(Collections.singletonList(e.getMessage()))
                        .timestamp(LocalDate.now().toString())
                        .build()
        );
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Mono<ErrorResponse> handleException(Exception e) {

        return Mono.just(
               ErrorResponse.builder()
                                .code(GATEWAY_INTERNAL_SERVER_ERROR.getCode())
                                .type(SYSTEM)
                                .message(GATEWAY_INTERNAL_SERVER_ERROR.getMessage())
                                .details(Collections.singletonList(e.getMessage()))
                                .timestamp(LocalDate.now().toString())
                                .build()
        );
    }
}

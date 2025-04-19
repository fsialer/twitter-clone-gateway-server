package com.fernando.ms.gateway.app.controllers;

import com.fernando.ms.gateway.app.exceptions.*;
import com.fernando.ms.gateway.app.dto.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;

import static com.fernando.ms.gateway.app.enums.ErrorType.FUNCTIONAL;
import static com.fernando.ms.gateway.app.enums.ErrorType.SYSTEM;
import static com.fernando.ms.gateway.app.utils.ErrorCatalog.*;
import static org.springframework.http.HttpStatus.*;


@RestControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(UserFallBackException.class)
    public Mono<ErrorResponse> handleUserFallBackException(UserFallBackException e){
        return Mono.just(
                ErrorResponse.builder()
                        .code(USER_FALLBACK.getCode())
                        .type(FUNCTIONAL)
                        .message(USER_FALLBACK.getMessage())
                        .timestamp(LocalDate.now().toString())
                        .build()
        );
    }

    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(PostFallBackException.class)
    public Mono<ErrorResponse> handlePostFallBackException(PostFallBackException e){
        return Mono.just(
                ErrorResponse.builder()
                        .code(POST_FALLBACK.getCode())
                        .type(FUNCTIONAL)
                        .message(POST_FALLBACK.getMessage())
                        .timestamp(LocalDate.now().toString())
                        .build()
        );
    }

    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(CommentFallBackException.class)
    public Mono<ErrorResponse> handleCommentFallBackException(CommentFallBackException e){
        return Mono.just(
                ErrorResponse.builder()
                        .code(COMMENT_FALLBACK.getCode())
                        .type(FUNCTIONAL)
                        .message(COMMENT_FALLBACK.getMessage())
                        .timestamp(LocalDate.now().toString())
                        .build()
        );
    }

    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(LikeFallBackException.class)
    public Mono<ErrorResponse> handleLikeFallBackException(LikeFallBackException e){
        return Mono.just(
                ErrorResponse.builder()
                        .code(LIKE_FALLBACK.getCode())
                        .type(FUNCTIONAL)
                        .message(LIKE_FALLBACK.getMessage())
                        .timestamp(LocalDate.now().toString())
                        .build()
        );
    }

    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(FollowerFallBackException.class)
    public Mono<ErrorResponse> handleFollowerFallBackException(FollowerFallBackException e){
        return Mono.just(
                ErrorResponse.builder()
                        .code(FOLLOWER_FALLBACK.getCode())
                        .type(FUNCTIONAL)
                        .message(FOLLOWER_FALLBACK.getMessage())
                        .timestamp(LocalDate.now().toString())
                        .build()
        );
    }

//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
//    public Mono<ErrorResponse> handleUnauthorizedException(WebClientResponseException.Unauthorized e){
//        return Mono.just(
//                ErrorResponse.builder()
//                        .code(UN_AUTHORIZATION.getCode())
//                        .type(FUNCTIONAL)
//                        .message(UN_AUTHORIZATION.getMessage())
//                        .timestamp(LocalDate.now().toString())
//                        .details(Collections.singletonList(e.getMessage()))
//                        .build()
//        );
//    }


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

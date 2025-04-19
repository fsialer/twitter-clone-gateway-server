package com.fernando.ms.gateway.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController
public class AuthorizationController {

    @GetMapping("/authorized")
    public Mono<Map<String,String>>authorized(@RequestParam String code){
        return  Mono.just(Collections.singletonMap("code",code));
    }
}

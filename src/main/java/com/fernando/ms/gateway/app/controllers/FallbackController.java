package com.fernando.ms.gateway.app.controllers;

import com.fernando.ms.gateway.app.exceptions.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {
    @GetMapping("/fallback/user")
    public Mono<ResponseEntity<UserFallBackException>> userFallback() {
        return Mono.error(UserFallBackException::new);
    }

    @PostMapping("/fallback/user")
    public Mono<ResponseEntity<UserFallBackException>> userFallback2() {
        return Mono.error(UserFallBackException::new);
    }

    @GetMapping("/fallback/post")
    public Mono<ResponseEntity<PostFallBackException>> postFallback(Throwable throwable) {
        return Mono.error(PostFallBackException::new);
    }

    @PostMapping("/fallback/post")
    public Mono<ResponseEntity<PostFallBackException>> postFallback2(Throwable throwable) {
        return Mono.error(PostFallBackException::new);
    }

    @GetMapping("/fallback/comment")
    public Mono<ResponseEntity<CommentFallBackException>> commentFallback() {
        return Mono.error(CommentFallBackException::new);
    }

    @PostMapping("/fallback/comment")
    public Mono<ResponseEntity<CommentFallBackException>> commentFallback2() {
        return Mono.error(CommentFallBackException::new);
    }

    @GetMapping("/fallback/like")
    public Mono<ResponseEntity<LikeFallBackException>> likeFallback() {
        return Mono.error(LikeFallBackException::new);
    }

    @PostMapping("/fallback/like")
    public Mono<ResponseEntity<LikeFallBackException>> likeFallback2() {
        return Mono.error(LikeFallBackException::new);
    }

    @GetMapping("/fallback/follower")
    public Mono<ResponseEntity<FollowerFallBackException>> followerFallback() {
        return Mono.error(FollowerFallBackException::new);
    }

    @PostMapping("/fallback/follower")
    public Mono<ResponseEntity<FollowerFallBackException>> followerFallback2() {
        return Mono.error(FollowerFallBackException::new);
    }
}

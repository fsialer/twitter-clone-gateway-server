package com.fernando.ms.gateway.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    @GetMapping("/user")
    public ResponseEntity<?> user( ){
        HashMap<String,String> res=new HashMap<>();
        res.put("message","user");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/admin")
    public ResponseEntity<?> admin(){
        HashMap<String,String> res=new HashMap<>();
        res.put("message","admim");
        return ResponseEntity.ok(res);
    }
}

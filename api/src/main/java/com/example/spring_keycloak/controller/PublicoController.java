package com.example.spring_keycloak.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/publico")
public class PublicoController {
    @RequestMapping(method=RequestMethod.GET)
    public ResponseEntity<?> requestMethodName(@RequestParam String param) {
        return ResponseEntity.ok().body("endpoint publico");
    }
    
}

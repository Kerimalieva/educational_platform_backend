package com.onlinelearning.controller;

import com.onlinelearning.dto.AuthRequest;
import com.onlinelearning.dto.AuthResponse;
import com.onlinelearning.dto.LoginRequest;
import com.onlinelearning.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173") // <- адрес фронтенда
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthRequest authRequest) {
        try {
            AuthResponse response = authService.register(authRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Преобразуем LoginRequest в AuthRequest для AuthService
            AuthRequest authRequest = new AuthRequest();
            authRequest.setEmail(loginRequest.getEmail());
            authRequest.setPassword(loginRequest.getPassword());

            AuthResponse response = authService.login(authRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }
}
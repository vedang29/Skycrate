package com.skycrate.backend.skycrateBackend.controller;

import com.skycrate.backend.skycrateBackend.dto.LoginRequest;
import com.skycrate.backend.skycrateBackend.security.JwtService;
import com.skycrate.backend.skycrateBackend.entity.User;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr(); // or use request.getEmail() as key

        if (rateLimiterService.isBlocked(ip)) {
            return ResponseEntity.status(429).body("Too many login attempts. Please try again later.");
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            rateLimiterService.recordFailedAttempt(ip);
            return ResponseEntity.status(401).body("Invalid credentials.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        rateLimiterService.resetAttempts(ip);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok().body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Client-side token deletion (or implement blacklist)
        return ResponseEntity.ok("Logged out (client should delete token)");
    }
}
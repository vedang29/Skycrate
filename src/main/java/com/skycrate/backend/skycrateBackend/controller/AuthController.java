package com.skycrate.backend.skycrateBackend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.skycrate.backend.skycrateBackend.dto.LoginUserDto;
import com.skycrate.backend.skycrateBackend.dto.RegisterUserDto;
import com.skycrate.backend.skycrateBackend.models.User;
import com.skycrate.backend.skycrateBackend.responses.LoginResponse;
import com.skycrate.backend.skycrateBackend.services.AuthenticationService;
import com.skycrate.backend.skycrateBackend.services.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/api")
@RestController
public class AuthController {
   
    private final JwtService jwtService;
    private AuthenticationService authenticationService;

    public AuthController(JwtService jwtService,AuthenticationService authenticationService){
        this.jwtService=jwtService;
        this.authenticationService=authenticationService;
    }

    @GetMapping("/test")
    public String teString(@RequestParam String param) {
        return new String();
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> LoginController(@RequestBody LoginUserDto entity) {

        User authenticatedUser=authenticationService.authenticate(entity);
        String jwtToken=jwtService.generateToken(authenticatedUser);
        
        LoginResponse loginResponse=new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirtationTime());
        return ResponseEntity.ok(loginResponse);
    }
    
    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto entity) {
        User registeredUser=authenticationService.signUp(entity);

        
        return ResponseEntity.ok(registeredUser);
    }
    

    
}

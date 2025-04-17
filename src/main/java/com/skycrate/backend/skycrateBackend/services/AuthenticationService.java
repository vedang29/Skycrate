package com.skycrate.backend.skycrateBackend.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skycrate.backend.skycrateBackend.dto.LoginUserDto;
import com.skycrate.backend.skycrateBackend.dto.RegisterUserDto;
import com.skycrate.backend.skycrateBackend.models.User;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import com.skycrate.backend.skycrateBackend.utils.RSAKeyUtil;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signUp(RegisterUserDto inputUser) {
        User user = new User(
                inputUser.getFirstname(),
                inputUser.getLastname(),
                inputUser.getEmail(),
                passwordEncoder.encode(inputUser.getPassword())
        );

        try {
            KeyPair keyPair = RSAKeyUtil.generateKeyPair();
            user.setPublicKey(keyPair.getPublic().getEncoded());
            user.setPrivateKey(keyPair.getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto inputUser) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(inputUser.getEmail(), inputUser.getPassword())
        );

        return userRepository.findByEmail(inputUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
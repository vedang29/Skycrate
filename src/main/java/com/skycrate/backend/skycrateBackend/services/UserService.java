package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.dto.SignupRequest;
import com.skycrate.backend.skycrateBackend.entity.User;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.MessageDigest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(SignupRequest request) {
        if (isPasswordPwned(request.getPassword())) {
            throw new IllegalArgumentException("Password has been compromised in data breaches.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    private boolean isPasswordPwned(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(password.getBytes());
            String fullHash = String.format("%040x", new BigInteger(1, hash)).toUpperCase();
            String prefix = fullHash.substring(0, 5);
            String suffix = fullHash.substring(5);

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject("https://api.pwnedpasswords.com/range/" + prefix, String.class);

            return response != null && response.contains(suffix);
        } catch (Exception e) {
            return false; // If API fails, allow but log in production
        }
    }
}
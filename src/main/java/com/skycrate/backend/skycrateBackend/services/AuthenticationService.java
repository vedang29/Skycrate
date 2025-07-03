package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.dto.LoginUserDto;
import com.skycrate.backend.skycrateBackend.dto.RegisterUserDto;
import com.skycrate.backend.skycrateBackend.entity.User;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import com.skycrate.backend.skycrateBackend.utils.EncryptionUtil;
import com.skycrate.backend.skycrateBackend.utils.RSAKeyUtil;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 AuthenticationManager authenticationManager,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signUp(RegisterUserDto inputUser) {
        // Generate RSA key pair
        KeyPair keyPair;
        try {
            keyPair = RSAKeyUtil.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }

        // Encrypt private key using password-derived AES key
        byte[] salt = EncryptionUtil.generateSalt();
        byte[] iv = EncryptionUtil.generateIv();
        byte[] encryptedPrivateKey;
        try {
            SecretKey aesKey = EncryptionUtil.deriveKey(inputUser.getPassword().toCharArray(), salt);
            encryptedPrivateKey = EncryptionUtil.encrypt(keyPair.getPrivate().getEncoded(), aesKey, iv);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt private key", e);
        }

        // Create user entity with encrypted private key, salt, and iv
        User user = User.builder()
                .fullname(inputUser.getFirstname() + " " + inputUser.getLastname())
                .username(inputUser.getUsername())
                .email(inputUser.getEmail())
                .password(passwordEncoder.encode(inputUser.getPassword()))
                .publicKey(keyPair.getPublic().getEncoded())
                .privateKey(encryptedPrivateKey)
                .privateKeySalt(salt)
                .privateKeyIv(iv)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Create HDFS directory in root with username
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            Path userDir = new Path("/" + savedUser.getUsername());
            if (!fs.exists(userDir)) {
                fs.mkdirs(userDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HDFS directory for user: " + savedUser.getUsername(), e);
        }

        return savedUser;
    }

    public User authenticate(LoginUserDto inputUser) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(inputUser.getEmail(), inputUser.getPassword())
        );

        return userRepository.findByEmail(inputUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
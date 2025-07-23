package com.skycrate.backend.skycrateBackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullname;

    @Lob
    private byte[] publicKey;

    @Lob
    private byte[] privateKey;

    @Lob
    @Column(nullable = false)
    private byte[] privateKeySalt;

    @Lob
    @Column(nullable = false)
    private byte[] privateKeyIv;

    @Builder
    public User(String email, String password, String username, String fullname,
                byte[] publicKey, byte[] privateKey,
                byte[] privateKeySalt, byte[] privateKeyIv) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.fullname = fullname;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.privateKeySalt = privateKeySalt;
        this.privateKeyIv = privateKeyIv;
    }

    // --- UserDetails interface methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // No roles assigned currently
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
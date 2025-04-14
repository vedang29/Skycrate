package  com.skycrate.backend.skycrateBackend.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;

@Table(name = "users")
@Entity
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String username;

    /*
    
   //Optional feature might add later

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name ="verification_expiry")
    private LocalDateTime verificationExpiry;

    */

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    
    
    public User(){
    }

    public User(String firstname,String lastname,String email,String password){
        this.username=firstname+lastname;
        this.email=email;
        this.password=password;
    }


    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of();
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    

    public void setFullname(String firstname,String lastname) {
        this.username=firstname+lastname;
    }

    public String getFullname(String firstname,String lastname){
        return this.username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

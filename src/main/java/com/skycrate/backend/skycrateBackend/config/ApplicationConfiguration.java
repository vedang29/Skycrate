package com.skycrate.backend.skycrateBackend.config;

import java.security.AuthProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.skycrate.backend.skycrateBackend.repository.UserRepository;

@Configuration
public class ApplicationConfiguration {
    private final UserRepository userRepository;
    public ApplicationConfiguration(UserRepository userRepository){
        this.userRepository=userRepository;

    }  
    @Bean
     UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @Bean
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authprovider=new DaoAuthenticationProvider();
        authprovider.setUserDetailsService(userDetailsService());
        authprovider.setPasswordEncoder(passwordEncoder());
        return authprovider;
    }
}

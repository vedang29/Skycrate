// package com.skycrate.backend.skycrateBackend.config;


// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//                 .csrf(csrf -> csrf.disable()) // Disable CSRF for testing APIs
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/api/hdfs/**").permitAll() // Allow HDFS endpoints
//                         .anyRequest().authenticated() // Everything else needs auth
//                 );

//         return http.build();
//     }
// }

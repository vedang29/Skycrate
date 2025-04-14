package com.skycrate.backend.skycrateBackend.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.skycrate.backend.skycrateBackend.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    

        private final HandlerExceptionResolver handlerExceptionResolver;
        private JwtService jwtService;
        private UserDetailsService userDetailsService;

        public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService,HandlerExceptionResolver handlerExceptionResolver){

            this.handlerExceptionResolver=handlerExceptionResolver;
            this.jwtService=jwtService;
            this.userDetailsService=userDetailsService;
        }

        @Override

    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader=request.getHeader("Authorization");
        if (authHeader==null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String userjwt=authHeader.substring(7);
            final String userEmail=jwtService.extractUsername(userjwt);
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            if(userEmail!=null && authentication==null){

                UserDetails userDetails=this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(userjwt, userDetails)) {
                    
                    UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                        );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);


                }

            }
            filterChain.doFilter(request, response);
            } 
        catch (Exception err) {
            handlerExceptionResolver.resolveException(request, response, null, err);
        }

        }


}

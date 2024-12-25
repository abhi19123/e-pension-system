package com.epension.service;

import com.epension.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.*;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Service
public class JwtService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final Key key;
    private final UserService userService;

    @Autowired
    public JwtService(@Value("${jwt.secret}") String secret, UserService userService) {
        this.secret = secret;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.userService = userService;
        logger.info("JwtService initialized with key");
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        logger.debug("Loading user details for phone number: {}", phoneNumber);
        User user = userService.findByPhoneNumber(phoneNumber);
        if (user == null) {
            logger.warn("User not found with phone number: {}", phoneNumber);
            throw new UsernameNotFoundException("User not found with phone number: " + phoneNumber);
        }
        logger.debug("User found: {}", user.getPhoneNumber());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public String generateToken(User user) {
        logger.debug("Generating token for user: {}", user.getPhoneNumber());
        String token = Jwts.builder()
                .setSubject(user.getPhoneNumber())
                .claim("role", user.getRole())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
        logger.debug("Token generated successfully");
        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            String username = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            logger.debug("Username extracted from token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Error extracting username from token", e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            logger.debug("Token validated successfully");
            return true;
        } catch (Exception e) {
            logger.error("JWT validation failed", e);
            return false;
        }
    }
}

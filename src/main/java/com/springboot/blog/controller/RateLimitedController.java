package com.springboot.blog.controller;

import com.springboot.blog.service.RedisRateLimiter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Ratelimiting")
@RestController
public class RateLimitedController {
    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Autowired
    private RedisRateLimiter rateLimiter;

    @GetMapping("/api/resource")
    public ResponseEntity<String> getResource(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract the Bearer token from the Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization header is missing or invalid");
        }

        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

        // Validate the token (in real application, you may want to extract the
        // client/user from the token)
        String clientId = extractClientIdFromToken(token);

        // Rate limit check
        if (!rateLimiter.isAllowed(clientId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded");
        }

        return ResponseEntity.ok("Request successful");
    }

    // A method to extract the client or user ID from the Bearer token
    private String extractClientIdFromToken(String token) {

        // Claims claims = Jwts.parser()
        // .setSigningKey(jwtSecret)
        // .parseClaimsJws(token)
        // .getBody();
        //
        //// Assuming the client ID is stored in the "clientId" field of the JWT payload
        // return claims.get("clientId", String.class);
        return token;
    }
}

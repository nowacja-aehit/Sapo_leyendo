package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.LoginRequest;
import com.mycompany.sapo_leyendo.dto.UserInfo;
import com.mycompany.sapo_leyendo.model.User;
import com.mycompany.sapo_leyendo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.Set;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<UserInfo> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("Attempting login for user: {}", loginRequest.getEmail());
            
            Authentication authenticationRequest =
                    UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword());
            
            Authentication authenticationResponse =
                    this.authenticationManager.authenticate(authenticationRequest);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationResponse);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found: " + loginRequest.getEmail()));
            
            Set<String> roles = user.getRoles() == null ? Collections.emptySet() : user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toSet());

            Set<String> permissions = user.getRoles() == null ? Collections.emptySet() : user.getRoles().stream()
                    .filter(role -> role.getPermissions() != null)
                    .flatMap(role -> role.getPermissions().stream())
                    .filter(permission -> permission != null)
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());

            logger.info("Login successful for user: {}", user.getEmail());

            return ResponseEntity.ok(new UserInfo(
                user.getId(), 
                user.getLogin(), 
                user.getFirstName(), 
                user.getLastName(),
                roles,
                permissions
            ));
        } catch (Exception e) {
            logger.error("Login failed", e);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Transactional
    public ResponseEntity<UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName(); // CustomUserDetailsService uses email as username
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        Set<String> roles = user.getRoles() == null ? Collections.emptySet() : user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles() == null ? Collections.emptySet() : user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission != null)
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new UserInfo(
            user.getId(), 
            user.getLogin(), 
            user.getFirstName(), 
            user.getLastName(),
            roles,
            permissions
        ));
    }
}

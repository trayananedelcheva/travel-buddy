package com.sap.travel_buddy.service;

import com.sap.travel_buddy.domain.User;
import com.sap.travel_buddy.dto.AuthenticationResponse;
import com.sap.travel_buddy.dto.LoginRequest;
import com.sap.travel_buddy.dto.RegisterRequest;
import com.sap.travel_buddy.repository.UserRepository;
import com.sap.travel_buddy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service за authentication и регистрация
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрация на нов потребител
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Проверка за съществуващ username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Проверка за съществуващ email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Създаване на нов потребител
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(User.Role.USER);
        user.setEnabled(true);

        user = userRepository.save(user);

        // Генериране на JWT токен
        String token = jwtUtil.generateToken(user);

        log.info("User registered successfully: {}", user.getUsername());

        return new AuthenticationResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    /**
     * Login на потребител
     */
    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        // Authentication
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Взимане на потребителя
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Обновяване на последен login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Генериране на JWT токен
        String token = jwtUtil.generateToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        return new AuthenticationResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}

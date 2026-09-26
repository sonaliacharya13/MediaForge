package com.example.mediaforge.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mediaforge.entity.User;
import com.example.mediaforge.repository.UserRepository;
import com.example.mediaforge.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            return "Username already exists";
        }

        User user = new User(
                username,
                passwordEncoder.encode(password)
        );

        userRepository.save(user);

        return "User registered successfully";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(()
                        -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid username or password");
        }

        return jwtService.generateToken(user.getUsername());
    }
}

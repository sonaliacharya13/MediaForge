package com.example.mediaforge.controller;

import com.example.mediaforge.entity.User;
import com.example.mediaforge.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}

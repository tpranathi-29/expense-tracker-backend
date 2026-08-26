package com.example.expense_tracker.controller;

import com.example.expense_tracker.jwt.JwtUtil;
import com.example.expense_tracker.model.User;
import com.example.expense_tracker.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "https://expense-tracker-frontend-vree.onrender.com")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Register User
    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Email already exists!";
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        userRepository.save(user);

        return "User registered successfully!";
    }
   // Login User
@PostMapping("/login")
public String loginUser(@RequestBody User user) {

    User existingUser = userRepository
            .findByEmail(user.getEmail())
            .orElse(null);

    if (existingUser == null) {
        return "User not found!";
    }

    System.out.println("Entered Password : " + user.getPassword());
    System.out.println("Stored Hash      : " + existingUser.getPassword());

    boolean match = passwordEncoder.matches(
            user.getPassword(),
            existingUser.getPassword()
    );

    System.out.println("Password Match : " + match);

    if (!match) {
        return "Invalid password!";
    }

    return jwtUtil.generateToken(existingUser.getEmail());
}
}
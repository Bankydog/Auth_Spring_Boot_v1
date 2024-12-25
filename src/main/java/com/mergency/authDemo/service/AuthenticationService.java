package com.mergency.authDemo.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mergency.authDemo.dto.LoginUserDto;
import com.mergency.authDemo.dto.RegisterUserDto;
import com.mergency.authDemo.dto.VerifyUserDto;
import com.mergency.authDemo.model.User;
import com.mergency.authDemo.repository.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input) {
        // Validate input
        if (input.getEmail() == null || input.getUsername() == null || input.getPassword() == null) {
            throw new IllegalArgumentException("Username, email, and password cannot be null.");
        }

        // Check if email already exists
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered.");
        }

        // Create user
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        // Send verification email
        sendVerificationEmail(user);

        // Save user
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        // Validate input
        if (input.getEmail() == null || input.getPassword() == null) {
            throw new IllegalArgumentException("Email and password cannot be null.");
        }

        // Find user
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));

        // Check if account is verified
        if (!user.isEnabled()) {
            throw new RuntimeException("Account is not verified.");
        }

        // Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        // Validate input
        if (input.getEmail() == null || input.getVerificationCode() == null) {
            throw new IllegalArgumentException("Email and verification code cannot be null.");
        }

        // Find user
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));

        // Check verification code
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired.");
        }
        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code.");
        }

        // Mark user as verified
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    public void resendVerificationCode(String email) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Check if user is already verified
        if (user.isEnabled()) {
            throw new RuntimeException("Account is already verified.");
        }

        // Generate new code and send email
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        sendVerificationEmail(user);

        // Save user
        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<h2>Welcome to our app!</h2>"
                + "<p>Your verification code is: <strong>" + user.getVerificationCode() + "</strong></p>"
                + "<p>This code will expire in 15 minutes.</p>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}

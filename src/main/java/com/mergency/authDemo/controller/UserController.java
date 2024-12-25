package com.mergency.authDemo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mergency.authDemo.model.User;
import com.mergency.authDemo.service.UserService;

@RequestMapping("/user")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> authenticateUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authentication is valid
        if (authentication == null || authentication.getPrincipal() == "anonymousUser") {
            return ResponseEntity.status(401).body("Unauthorized: No user is authenticated.");
        }

        // Retrieve current user
        User currentUser;
        try {
            currentUser = (User) authentication.getPrincipal();
        } catch (ClassCastException e) {
            return ResponseEntity.status(500).body("Error: Unable to retrieve authenticated user.");
        }

        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }
}

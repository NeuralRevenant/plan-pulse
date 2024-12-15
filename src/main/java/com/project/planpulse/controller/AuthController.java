package com.project.planpulse.controller;

import com.project.planpulse.util.JwtUtil;
import com.project.planpulse.model.User;
import com.project.planpulse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    // Signup
    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, String>> register(
            @RequestParam(name = "firstname") String firstname,
            @RequestParam(name = "lastname") String lastname,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "confirmPassword") String confirmPassword,
            @RequestParam(name = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        User registeredUser = userService.registerUserWithMultipart(firstname, lastname, username, email, password, confirmPassword, profileImage);
        String token = JwtUtil.generateToken(registeredUser.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        Map<String, String> responseBody = Map.of("token", "Bearer " + token, "userId", registeredUser.getId());
        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String identifier = credentials.get("identifier");
        String password = credentials.get("password");
        User user;
        if (isEmail(identifier)) {
            user = userService.authenticateByEmail(identifier, password);
        } else {
            user = userService.authenticateByUsername(identifier, password);
        }
        if (user == null) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = JwtUtil.generateToken(user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        Map<String, String> responseBody = Map.of("token", "Bearer " + token, "userId", user.getId());
        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, null); // clear the Authorization header
        Map<String, String> responseBody = Map.of("message", "Logged out successfully");
        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }

    // Forgot Password
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> request) throws RuntimeException {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        userService.initiatePasswordReset(email);
        // a generic message for security reasons
        return Map.of("message", "If an account with that email exists, a reset link has been sent.");
    }

    // Reset Password (after getting the token via email)
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("Token, new password, and confirm password are required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password and the new password must be the same");
        }
        userService.resetPasswordWithToken(token, newPassword);
        return Map.of("message", "Password has been reset successfully");
    }

    private boolean isEmail(String identifier) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return identifier.matches(emailRegex);
    }
}

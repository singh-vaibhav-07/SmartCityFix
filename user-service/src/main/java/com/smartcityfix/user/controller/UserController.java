package com.smartcityfix.user.controller;

import com.smartcityfix.common.dto.ApiResponse;
import com.smartcityfix.user.dto.AuthResponse;
import com.smartcityfix.user.dto.UserLoginRequest;
import com.smartcityfix.user.dto.UserRegistrationRequest;
import com.smartcityfix.user.dto.UserResponse;
import com.smartcityfix.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with CITIZEN role")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        UserResponse userResponse = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates credentials and returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthResponse authResponse = userService.loginUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns user details for the given ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Fetching user with id: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates user information for the given ID")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Updating user with id: {}", id);
        UserResponse userResponse = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
    }
}
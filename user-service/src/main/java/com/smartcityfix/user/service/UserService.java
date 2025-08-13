package com.smartcityfix.user.service;

import com.smartcityfix.user.dto.AuthResponse;
import com.smartcityfix.user.dto.UserLoginRequest;
import com.smartcityfix.user.dto.UserRegistrationRequest;
import com.smartcityfix.user.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse registerUser(UserRegistrationRequest request);

    AuthResponse loginUser(UserLoginRequest request);

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UUID id, UserRegistrationRequest request);
}
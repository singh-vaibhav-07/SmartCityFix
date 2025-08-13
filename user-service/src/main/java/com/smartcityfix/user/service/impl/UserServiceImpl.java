package com.smartcityfix.user.service.impl;

import com.smartcityfix.common.event.UserRegisteredEvent;
import com.smartcityfix.common.exception.ResourceNotFoundException;
import com.smartcityfix.user.dto.AuthResponse;
import com.smartcityfix.user.dto.UserLoginRequest;
import com.smartcityfix.user.dto.UserRegistrationRequest;
import com.smartcityfix.user.dto.UserResponse;
import com.smartcityfix.user.exception.InvalidCredentialsException;
import com.smartcityfix.user.exception.UserAlreadyExistsException;
import com.smartcityfix.user.messaging.UserEventPublisher;
import com.smartcityfix.user.model.User;
import com.smartcityfix.user.model.UserRole;
import com.smartcityfix.user.repository.UserRepository;
import com.smartcityfix.user.security.JwtTokenProvider;
import com.smartcityfix.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("User with email {} already exists", request.getEmail());
                throw new UserAlreadyExistsException(request.getEmail());
            }

            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .phone(request.getPhone())
                    .role(UserRole.CITIZEN)
                    .build();

            // Save the user first
            User savedUser = userRepository.save(user);

            // Flush to ensure the entity is persisted
            userRepository.flush();

            // Store the ID in a final variable for use in the lambda
            final UUID userId = savedUser.getId();

            // Fetch the user again to get the updated entity with timestamps
            User refreshedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            log.info("User registered successfully with id: {}", refreshedUser.getId());

            // Publish user registered event
            eventPublisher.publishUserRegisteredEvent(
                    new UserRegisteredEvent(refreshedUser.getId(), refreshedUser.getEmail(), refreshedUser.getName()));

            return mapToUserResponse(refreshedUser);
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error registering user", e);
            throw e;
        }
    }

    @Override
    public AuthResponse loginUser(UserLoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException());

            String jwt = tokenProvider.generateToken(authentication);
            log.info("User logged in successfully: {}", request.getEmail());

            return AuthResponse.builder()
                    .token(jwt)
                    .user(mapToUserResponse(user))
                    .build();
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user with id: {}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            log.info("User found: {}", user.getEmail());
            return mapToUserResponse(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserRegistrationRequest request) {
        log.info("Updating user with id: {}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            // Check if email is being changed and if it's already taken
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                log.warn("Email {} is already taken", request.getEmail());
                throw new UserAlreadyExistsException(request.getEmail());
            }

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());

            // Only update password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            User updatedUser = userRepository.save(user);
            userRepository.flush();

            // Store the ID in a final variable for use in the lambda
            final UUID userId = updatedUser.getId();

            // Fetch the user again to get the updated entity with timestamps
            User refreshedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            log.info("User updated successfully: {}", refreshedUser.getId());

            return mapToUserResponse(refreshedUser);
        } catch (ResourceNotFoundException | UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating user with id: {}", id, e);
            throw e;
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
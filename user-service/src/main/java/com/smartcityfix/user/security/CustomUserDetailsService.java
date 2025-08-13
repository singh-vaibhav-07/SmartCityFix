package com.smartcityfix.user.security;

import com.smartcityfix.common.exception.ResourceNotFoundException;
import com.smartcityfix.user.model.User;
import com.smartcityfix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            log.info("User found with email: {}", email);
            return UserPrincipal.create(user);
        } catch (Exception e) {
            log.error("Error loading user by email: {}", email, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String id) {
        try {
            User user = userRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            log.info("User found with id: {}", id);
            return UserPrincipal.create(user);
        } catch (Exception e) {
            log.error("Error loading user by id: {}", id, e);
            throw e;
        }
    }
}
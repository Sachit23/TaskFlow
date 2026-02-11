package com.userservice.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.userservice.userservice.dto.UserResponse;
import com.userservice.userservice.entity.User;
import com.userservice.userservice.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public UserResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email does not exist!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return mapToResponse(user);
    }

    public User getUserById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse mapToResponse(User user) {
        UserResponse refinedUserResponse = new UserResponse();
        refinedUserResponse.setId(user.getId());
        refinedUserResponse.setName(user.getName());
        refinedUserResponse.setEmail(user.getEmail());
        return refinedUserResponse; 
    }

}


package com.userservice.userservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.userservice.userservice.entity.User;
import com.userservice.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegister_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password123");
        when(userRepository.findByEmail(eq("test@example.com"))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.register(user);

        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_EmailExists_ThrowsException() {
        User user = new User();
        user.setEmail("exists@example.com");

        when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.register(user));
    }
}
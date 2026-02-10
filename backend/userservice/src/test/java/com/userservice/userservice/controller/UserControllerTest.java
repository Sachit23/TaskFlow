package com.userservice.userservice.controller;

//import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.userservice.entity.User;
import com.userservice.userservice.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Sachit");
        user.setEmail("sachit@test.com");
        user.setPassword("password123");

        when(userService.register(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sachit"))
                .andExpect(jsonPath("$.email").value("sachit@test.com"));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        User loginRequest = new User();
        loginRequest.setEmail("alice@test.com");
        loginRequest.setPassword("12345");

        User loggedInUser = new User();
        loggedInUser.setId(1L);
        loggedInUser.setName("Alice");
        loggedInUser.setEmail("alice@test.com");

        when(userService.login(
                eq("alice@test.com"),
                eq("12345")
        )).thenReturn(loggedInUser);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@test.com"));
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        User loginRequest = new User();
        loginRequest.setEmail("alice@test.com");
        loginRequest.setPassword("wrongpassword");

        when(userService.login(
                eq("alice@test.com"),
                eq("wrongpassword")
        )).thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        ));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}

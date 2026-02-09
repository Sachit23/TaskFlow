package com.userservice.userservice.controller;

//import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.userservice.entity.User;
import com.userservice.userservice.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private tools.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new tools.jackson.databind.ObjectMapper();
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
}

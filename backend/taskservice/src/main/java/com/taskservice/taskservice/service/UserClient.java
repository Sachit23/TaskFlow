package com.taskservice.taskservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${userservice.url}")
    private String userServiceUrl;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validateUser(Long userId) {
        try {
            restTemplate.getForObject(
                userServiceUrl + "/api/users/" + userId,
                Object.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Assigned user does not exist");
        }
    }
}

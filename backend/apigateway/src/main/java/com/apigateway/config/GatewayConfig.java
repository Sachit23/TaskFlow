// package com.apigateway.config;

// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// /**
//  * Gateway Route Configuration
//  * Defines routes for routing requests to different microservices
//  */
// @Configuration
// public class GatewayConfig {

//     @Bean
//     public RouteLocator customRoutes(RouteLocatorBuilder builder) {
//         return builder.routes()
//                 .route("userservice", r -> r
//                         .path("/api/users/**")
//                         .filters(f -> f.rewritePath("/api/users(?<segment>/?.*)", "${segment}"))
//                         .uri("${USERSERVICE_URL}"))
//                 .route("taskservice", r -> r
//                         .path("/api/tasks/**")
//                         .filters(f -> f.rewritePath("/api/tasks(?<segment>/?.*)", "${segment}"))
//                         .uri("${TASKSERVICE_URL}"))
//                 .build();
//     }

// }

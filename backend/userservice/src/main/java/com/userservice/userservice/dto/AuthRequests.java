package com.userservice.userservice.dto;
import jakarta.validation.constraints.*;
public final class AuthRequests {
 public record Register(@NotBlank @Size(max=80) String name,@NotBlank @Email @Size(max=254) String email,@NotNull @Size(min=10,max=72) String password) {}
 public record Login(@NotBlank @Email @Size(max=254) String email,@NotNull @Size(min=1,max=72) String password) {}
 public record Profile(@NotBlank @Size(max=80) String name) {}
 public record Password(@NotBlank String currentPassword,@NotNull @Size(min=10,max=72) String newPassword) {}
 public record AuthResponse(String token, UserResponse user) {}
}

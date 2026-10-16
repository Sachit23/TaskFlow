package com.userservice.userservice.entity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="user_sessions") @Getter @Setter
public class UserSession {
 @Id @Column(length=64) private String tokenHash;
 @Column(nullable=false) private Long userId;
 @Column(nullable=false) private Instant expiresAt;
}

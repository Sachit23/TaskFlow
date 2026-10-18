package com.taskservice.taskservice.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name="projects") @Getter @Setter
public class Project {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long ownerId;
 @Column(nullable=false,length=80) private String name;
 @Column(length=1000) private String description;
 @Column(nullable=false,length=7) private String color;
 @Column(nullable=false) private boolean archived;
 @Column(nullable=false) private LocalDateTime createdAt;
 @PrePersist void create(){createdAt=LocalDateTime.now(java.time.Clock.systemUTC());}
}

package com.userservice.userservice.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="users") @Getter @Setter
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=254) private String email;
 @JsonIgnore @Column(nullable=false) private String password;
 @Column(nullable=false,length=80) private String name;
}

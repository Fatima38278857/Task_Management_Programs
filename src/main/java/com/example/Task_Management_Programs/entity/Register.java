package com.example.Task_Management_Programs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "register")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
@NoArgsConstructor
@Data
@Builder
public class Register {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @OneToOne(mappedBy = "register")
    private UserEntity userEntity;

    public Register(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

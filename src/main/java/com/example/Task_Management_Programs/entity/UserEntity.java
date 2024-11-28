package com.example.Task_Management_Programs.entity;


import com.example.Task_Management_Programs.enumm.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;


    @Column(unique = true, nullable = false)
    private String email;

    private String username;

    @OneToOne
    @JoinColumn(name = "register_id", referencedColumnName = "id")
    @JsonIgnore
    private Register register;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<TaskEntity> taskEntity;
    @OneToOne
    @JoinColumn(name = "task_id")
    private TaskEntity currentTask;



    public UserEntity(String email, Role role, Register register) {
        this.email = email;
        this.register = register;
        this.role = role;
        System.out.println("UserEntity created with email: " + email + ", role: " + role);
        this.username = username;
        this.taskEntity = new ArrayList<>();  // Инициализация пустого списка задач
        this.currentTask = currentTask; // Задача назначена при создании
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return "";
    }


    @Override
    @JsonIgnore
    public String getUsername() {
        return "";
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

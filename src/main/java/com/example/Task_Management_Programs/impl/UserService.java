package com.example.Task_Management_Programs.impl;


import com.example.Task_Management_Programs.dto.UserDTO;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.security.RegistrationRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {

    UserEntity getAuthenticatedUser();
    UserDTO getUser();
    UserEntity getUser(String email);
    ResponseEntity<String> registerUser(RegistrationRequest request);
    UserEntity findById(Long id);
   // public UserEntity addUser(UserDTO userDTO);
}

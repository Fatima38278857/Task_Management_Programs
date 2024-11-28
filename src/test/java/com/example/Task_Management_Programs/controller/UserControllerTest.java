package com.example.Task_Management_Programs.controller;

import com.example.Task_Management_Programs.claass.UserProfileRequest;
import com.example.Task_Management_Programs.entity.Register;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.enumm.Role;
import com.example.Task_Management_Programs.repository.RegisterRepository;
import com.example.Task_Management_Programs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import static org.mockito.ArgumentMatchers.any;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private RegisterRepository registerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private UserProfileRequest request;
    private Principal principal;

    @BeforeEach
    void setUp() {
        request = new UserProfileRequest("user@example.com", Role.AUTHOR);
        principal = () -> "testuser";
    }

    @Test
    void createProfile_Success() {
        Register register = new Register("testuser", "password");

        when(registerRepository.findByUsername(principal.getName())).thenReturn(Optional.of(register));
        when(userRepository.findByRegister(register)).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.createProfile(request, principal);

        assertEquals("Профиль пользователя успешно создан", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void createProfile_ProfileAlreadyExists() {
        Register register = new Register("testuser", "password");
        UserEntity existingUser = new UserEntity("user@example.com", Role.AUTHOR, register);

        when(registerRepository.findByUsername(principal.getName())).thenReturn(Optional.of(register));
        when(userRepository.findByRegister(register)).thenReturn(Optional.of(existingUser));

        ResponseEntity<String> response = userController.createProfile(request, principal);

        assertEquals("Профиль пользователя уже существует", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void createProfile_DataIntegrityViolation() {
        Register register = new Register("testuser", "password");

        when(registerRepository.findByUsername(principal.getName())).thenReturn(Optional.of(register));
        when(userRepository.findByRegister(register)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException(""));

        ResponseEntity<String> response = userController.createProfile(request, principal);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Не удалось сохранить пользователя из-за нарушения ограничений: ", response.getBody());
    }
}
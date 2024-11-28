package com.example.Task_Management_Programs.controller;


import com.example.Task_Management_Programs.claass.UserProfileRequest;
import com.example.Task_Management_Programs.dto.UserDTO;
import com.example.Task_Management_Programs.entity.Register;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.impl.UserService;
import com.example.Task_Management_Programs.mapper.UserMapper;
import com.example.Task_Management_Programs.mapperr.UserMapperr;
import com.example.Task_Management_Programs.repository.RegisterRepository;
import com.example.Task_Management_Programs.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(value = "http://localhost:8080")
@RequestMapping("/users")
@Tag(name = "Пользователи")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegisterRepository registerRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserMapperr userMapperr;



    @Operation(summary = "Создать профиль пользователя", description = "Создает новый профиль пользователя, если он не существует.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль пользователя успешно создан"),
            @ApiResponse(responseCode = "400", description = "Профиль пользователя уже существует", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @PostMapping("/createProfile")
    public ResponseEntity<String> createProfile(@RequestBody UserProfileRequest request, Principal principal) {

        System.out.println("Request email: " + request.getEmail());
        System.out.println("Request role: " + request.getRole());
        System.out.println("Principal username: " + principal.getName());

        // Находим регистрацию по имени пользователя из principal (которое извлекается из токена)
        Register register = registerRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + principal.getName()));

        // Проверяем, есть ли уже профиль пользователя
        if (userRepository.findByRegister(register).isPresent()) {
            return ResponseEntity.badRequest().body("Профиль пользователя уже существует");
        }

        // Создаем учетные данные
        UserEntity user = new UserEntity(request.getEmail(), request.getRole(), register);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed to save user due to constraint violation: " + ex.getMessage());

        }
        return ResponseEntity.ok("Профиль пользователя успешно создан");
    }
    @Operation(summary = "Получить данные пользователя по ID", description = "Возвращает данные пользователя на основе ID пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserEntity userEntity = userService.findById(id);
        UserDTO userDTO = userMapperr.toDTO(userEntity);
        return ResponseEntity.ok(userDTO);
    }
}

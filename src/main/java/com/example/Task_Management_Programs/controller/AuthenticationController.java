package com.example.Task_Management_Programs.controller;


import com.example.Task_Management_Programs.impl.UserService;
import com.example.Task_Management_Programs.repository.UserRepository;
import com.example.Task_Management_Programs.security.AuthenticationRequest;
import com.example.Task_Management_Programs.security.JwtUtil;
import com.example.Task_Management_Programs.security.RegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private UserRepository userRepository;

    @Operation(summary = "Создание токена для пользователя",
            description = "Аутентифицирует пользователя и возвращает JWT токен.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "Недействительные учетные данные",
                    content = @Content)
    })
    @PostMapping("/create")
    public String createToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.login(),
                            authenticationRequest.password()));
            if (authentication.isAuthenticated()) {
                System.out.println("Аутентификация успешна для пользователя: " + authenticationRequest.login());
                String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
                System.out.println("Generated token: " + token);
                return token;
            } else {
                throw new Exception("Недействительные учетные данные");
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw new Exception("Недействительные учетные данные", e);
        }

    }
    @Operation(summary = "Регистрация нового пользователя",
            description = "Регистрирует нового пользователя и возвращает результат регистрации.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Ошибка регистрации",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistrationRequest request) {
        return userService.registerUser(request);
    }
}

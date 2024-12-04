package com.example.Task_Management_Programs.service;


import com.example.Task_Management_Programs.dto.UserDTO;
import com.example.Task_Management_Programs.entity.Register;
import com.example.Task_Management_Programs.entity.UserEntity;
import com.example.Task_Management_Programs.impl.UserService;
import com.example.Task_Management_Programs.mapperr.UserMapperr;
import com.example.Task_Management_Programs.repository.RegisterRepository;
import com.example.Task_Management_Programs.repository.UserRepository;
import com.example.Task_Management_Programs.security.RegistrationRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
/**
 * Сервис для управления пользователями.
 *
 * <p>Этот класс предоставляет реализацию интерфейса {@link UserService} для выполнения операций,
 * связанных с пользователями, таких как аутентификация, регистрация, получение данных пользователя и управление учетными записями.</p>
 *
 * Основные функции:
 * - Получение текущего аутентифицированного пользователя.
 * - Преобразование данных пользователя в DTO.
 * - Регистрация новых пользователей с проверкой уникальности логина.
 * - Получение пользователей по их идентификаторам или адресам электронной почты.
 *
 * Используемые зависимости:
 * - {@link UserRepository} — для работы с пользователями в базе данных.
 * - {@link UserMapperr} — для преобразования сущностей пользователей в DTO.
 * - {@link BCryptPasswordEncoder} — для хэширования паролей при регистрации.
 * - {@link RegisterRepository} — для управления учетными записями регистрации.
 *
 * <p>Класс также обеспечивает проверку аутентификации и прав доступа через SecurityContext.</p>
 */
@Service
public class UserImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserImpl.class);
    private final UserRepository userRepository;
    private final UserMapperr userMapperr;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RegisterRepository registerRepository;

    @Autowired
    public UserImpl(UserRepository userRepository, UserMapperr userMapperr, BCryptPasswordEncoder passwordEncoder, RegisterRepository registerRepository) {
        this.userRepository = userRepository;
        this.userMapperr = userMapperr;
        this.passwordEncoder = passwordEncoder;
        this.registerRepository = registerRepository;
    }

    /**
     * Получает аутентифицированного пользователя из SecurityContext.
     * Если пользователь не аутентифицирован, возвращает null.
     */
    @Override
    public UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByRegisterUsername(username);
    }

    /**
     * Возвращает текущего аутентифицированного пользователя.
     * Если пользователь не найден или не аутентифицирован, возвращает null.
     */
    private UserEntity currentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return getUser(((UserDetails) authentication.getPrincipal()).getUsername());
        } catch (NullPointerException е) {
            е.printStackTrace();
            log.info("Пользователь не аутентифицирован");
        }
        return null;
    }

    /**
     * Преобразует текущего аутентифицированного пользователя в DTO-объект.
     * Если пользователь не найден, выбрасывает исключение.
     */
    @Override
    public UserDTO getUser() {
        UserEntity user = currentUser();

        if (user != null) {
            return userMapperr.toDTO(user);
        }
        throw new RuntimeException("Нет авторизованного пользователя");
    }
    /**
     * Получает пользователя по адресу электронной почты.
     * Если пользователь с указанным email не найден, возвращает null.
     */
    @Override
    public UserEntity getUser(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Регистрация нового пользователя. Проверяет, что логин и пароли корректны,
     * и что пользователь с таким логином не зарегистрирован.
     * @param request Объект, содержащий данные регистрации.
     * @return ResponseEntity с сообщением об успешной регистрации или ошибкой.
     */
    @Override
    public ResponseEntity<String> registerUser(RegistrationRequest request) {
        String username = request.login(); // Используем правильный метод геттера
        String password = request.password();
        String confirmPassword = request.confirmPassword();

        // Проверяем на пустые значения
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }
        if (userRepository.findByUsername(request.login()).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }
        Register register = new Register(request.login(), passwordEncoder.encode(request.password()));
        registerRepository.save(register);

        return ResponseEntity.ok("Пользователь успешно зарегистрирован! Далее пройдите аутентификацию и создайте учетную запись");
    }
    /**
     * Находит пользователя по ID. Если пользователь не найден, выбрасывает исключение.
     * @param id Идентификатор пользователя.
     * @return Найденный пользователь.
     */
    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

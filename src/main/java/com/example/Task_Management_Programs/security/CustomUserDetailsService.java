package com.example.Task_Management_Programs.security;


import com.example.Task_Management_Programs.entity.Register;
import com.example.Task_Management_Programs.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
/**
 * Сервис для загрузки данных пользователей из базы данных для аутентификации.
 *
 * <p>Этот класс реализует интерфейс {@link UserDetailsService}, который используется Spring Security
 * для загрузки данных пользователя (например, имени пользователя и пароля) при аутентификации.</p>
 *
 * Основные функции:
 * - Поиск пользователя по имени пользователя в репозитории.
 * - Преобразование данных пользователя в объект {@link UserDetails}, который используется Spring Security.
 *
 * Используемые зависимости:
 * - {@link RegisterRepository} — репозиторий для работы с данными пользователей.
 *
 * Используемые аннотации:
 * - {@link Service} — обозначает, что класс является компонентом сервисного слоя и может быть внедрён в другие компоненты.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final RegisterRepository registerRepository;

    @Autowired
    public CustomUserDetailsService(RegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    /**
     * Загружает данные пользователя по имени пользователя (логину).
     *
     * <p>Этот метод ищет пользователя в репозитории по имени пользователя. Если пользователь найден,
     * он преобразуется в объект {@link UserDetails}, который используется Spring Security для проверки аутентификации.</p>
     *
     * @param username Имя пользователя, по которому выполняется поиск.
     * @return Объект {@link UserDetails}, содержащий данные для аутентификации.
     * @throws UsernameNotFoundException Если пользователь с указанным именем не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Register user = registerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("Пользователь не найден: " + username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        // Пользователь найден, выводим его данные
        System.out.println("Попытка загрузить пользователя по имени пользователя: " + username);
        System.out.println("Найден пользователь: " + username + ", пароль: " + user.getPassword());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}





package com.example.Task_Management_Programs.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Конфигурация безопасности для приложения.
 *
 * <p>Класс настраивает безопасность с использованием Spring Security, включая фильтрацию JWT-токенов,
 * настройку разрешений для маршрутов и отключение состояния сессии для работы с REST API.</p>
 *
 * Основные функции:
 * - Настройка безопасности с использованием цепочки фильтров (SecurityFilterChain).
 * - Настройка разрешений для маршрутов (открытые и защищенные маршруты).
 * - Интеграция JWT-фильтра для обработки токенов.
 * - Подключение шифровальщика паролей (BCryptPasswordEncoder).
 * - Предоставление менеджера аутентификации (AuthenticationManager) для работы с токенами.
 *
 * Используемые аннотации:
 * - {@link Configuration} — обозначает, что класс является конфигурацией Spring.
 * - {@link EnableWebSecurity} — включает поддержку Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    /**
     * Конфигурирует цепочку фильтров безопасности приложения.
     *
     * <p>Отключает CSRF-защиту, настраивает разрешения для маршрутов и добавляет JWT-фильтр
     * для обработки токенов до выполнения стандартной аутентификации.</p>
     *
     * @param http Объект конфигурации {@link HttpSecurity}, предоставляемый Spring Security.
     * @return Настроенная цепочка фильтров безопасности.
     * @throws Exception Если настройка цепочки фильтров завершится с ошибкой.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/authenticate/**").permitAll().requestMatchers(
                                "/create",
                                "/register",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll().anyRequest().authenticated())
                .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    /**
     * Шифровальщик паролей с использованием BCrypt.
     *
     * <p>Используется для шифрования и проверки паролей пользователей при их сохранении и аутентификации.</p>
     *
     * @return Объект {@link BCryptPasswordEncoder}.
     */
    @Bean
    public  BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * Предоставляет менеджер аутентификации.
     *
     * <p>Используется для аутентификации пользователей и проверки их учетных данных.</p>
     *
     * @param authenticationConfiguration Конфигурация аутентификации Spring.
     * @return Объект {@link AuthenticationManager}.
     * @throws Exception Если создание менеджера аутентификации завершится ошибкой.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

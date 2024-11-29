package com.example.Task_Management_Programs.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

/**
 * Утилитарный класс для работы с JWT-токенами.
 *
 * <p>Этот класс предоставляет методы для создания, проверки и валидации JWT-токенов.
 * Используется в приложении для управления аутентификацией и авторизацией.</p>
 * <p>
 * Основные функции:
 * - Генерация JWT-токенов для пользователей.
 * - Извлечение данных из токена (имя пользователя, дата истечения и т.д.).
 * - Проверка валидности токенов и их срока действия.
 * <p>
 * Используемые аннотации:
 * - {@link Component} — позволяет использовать этот класс как Spring Bean.
 * - {@link Value} — позволяет загружать секретный ключ из `application.properties`.
 * - {@link PostConstruct} — выполняет инициализацию после создания бина.
 * <p>
 * Используемые библиотеки:
 * - `io.jsonwebtoken` для работы с JWT.
 */
@Component
public class JwtUtil {
    @Value("${secret.key}")
    private String SECRET_KEY;

    /**
     * Извлекает имя пользователя (subject) из JWT-токена.
     *
     * @param token JWT-токен.
     * @return Имя пользователя, извлеченное из токена.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия токена.
     *
     * @param token JWT-токен.
     * @return Дата истечения срока действия.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает указанное поле из токена с использованием функции разрешения (resolver).
     *
     * @param token          JWT-токен.
     * @param claimsResolver Функция для извлечения данных из объекта `Claims`.
     * @param <T>            Тип извлекаемого значения.
     * @return Значение, извлеченное из токена.
     */
    private <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }
    /**
     * Извлекает все данные (`Claims`) из токена.
     *
     * @param token JWT-токен.
     * @return Объект `Claims`, содержащий все данные токена.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
    /**
     * Генерирует JWT-токен для пользователя.
     *
     * @param userDetails Объект, содержащий информацию о пользователе.
     * @return Сгенерированный JWT-токен.
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(new DefaultClaims())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Токен действителен 10 часов
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token JWT-токен.
     * @return `true`, если токен истек; `false` в противном случае.
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    /**
     * Проверяет валидность токена.
     *
     * @param token JWT-токен.
     * @param userDetails Информация о пользователе.
     * @return `true`, если токен валиден и принадлежит указанному пользователю; `false` в противном случае.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    /**
     * Функциональный интерфейс для разрешения данных из объекта `Claims`.
     *
     * @param <T> Тип извлекаемого значения.
     */
    @FunctionalInterface
    private interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
    /**
     * Выводит секретный ключ в лог после инициализации бина.
     *
     * <p>Этот метод используется для проверки корректности загрузки секретного ключа.</p>
     */
    @PostConstruct
    public void init() {
        System.out.println("Secret Key: " + SECRET_KEY); // Или использовать системное логирование
    }
}

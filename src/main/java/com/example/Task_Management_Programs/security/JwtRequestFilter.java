package com.example.Task_Management_Programs.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


import java.io.IOException;

/**
 * Фильтр для обработки JWT-токенов в каждом HTTP-запросе.
 *
 * <p>Этот класс расширяет {@link OncePerRequestFilter}, чтобы гарантировать, что фильтрация выполняется только один раз
 * за запрос. Основная задача фильтра — извлечь JWT-токен из заголовка запроса, проверить его валидность, и если токен
 * корректный, настроить аутентификацию в контексте Spring Security.</p>
 *
 * Основные функции:
 * - Извлечение JWT-токена из заголовка `Authorization`.
 * - Проверка валидности токена (подписи, срока действия).
 * - Установка аутентификации в SecurityContext.
 * - Исключение публичных маршрутов (например, Swagger или страницы регистрации) из фильтрации.
 *

 * Используемые аннотации:
 * - {@link Component} — позволяет использовать этот класс как Spring Bean.
 *
 * Используемые зависимости:
 * - {@link JwtUtil} — для работы с токенами (извлечение данных, проверка валидности).
 * - {@link CustomUserDetailsService} — для загрузки данных пользователя из хранилища.
 *
 * Используемые библиотеки:
 * - `io.jsonwebtoken` для работы с JWT.
 * - `org.slf4j.Logger` для логирования.
 */

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }
    /**
     * Основной метод фильтрации HTTP-запросов.
     *
     * <p>Обрабатывает каждый запрос, проверяя наличие и валидность JWT-токена. Если токен валиден, устанавливает
     * аутентификацию в {@link SecurityContextHolder} для текущего запроса. Публичные маршруты исключаются из фильтрации.</p>
     *
     * @param request  Объект запроса {@link HttpServletRequest}.
     * @param response Объект ответа {@link HttpServletResponse}.
     * @param chain    Цепочка фильтров для передачи управления следующему фильтру.
     * @throws ServletException В случае ошибок обработки запроса.
     * @throws IOException      В случае ошибок ввода-вывода.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestUri = request.getRequestURI();

        // Исключение Swagger URL из фильтрации
        if (requestUri.startsWith("/swagger-ui") ||
                requestUri.startsWith("/v3/api-docs") ||
                requestUri.startsWith("/register") ||
                requestUri.startsWith("/create") ||
                requestUri.startsWith("/swagger-resources") ||
                requestUri.startsWith("/webjars")) {
            chain.doFilter(request, response);
            return;
        }
        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("Запрос обработан в JwtRequestFilter");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(jwtToken);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Аутентификация прошла успешно для пользователя: {}", username);
                    }
                }

            } catch (MalformedJwtException exception) {
                logger.error("JWT имеет неправильный формат.", exception);
                handleErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Неверный токен");
                return;
            } catch (ExpiredJwtException exception) {
                logger.error("JWT истек.", exception);
                handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Токен истек");
                return;
            } catch (Exception exception) {
                logger.error("Ошибка при обработке JWT.", exception);
                handleErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
                return;
            }
        }

        chain.doFilter(request, response);
    }
    /**
     * Обрабатывает ошибочный ответ.
     *
     * <p>Устанавливает HTTP-статус и возвращает JSON-объект с описанием ошибки.</p>
     *
     * @param response Объект ответа {@link HttpServletResponse}.
     * @param status   HTTP-статус ошибки.
     * @param message  Сообщение об ошибке.
     * @throws IOException Если возникает ошибка ввода-вывода.
     */
    private void handleErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

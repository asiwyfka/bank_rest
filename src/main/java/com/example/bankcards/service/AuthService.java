package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.bankcards.entity.RoleName.ROLE_USER;

/**
 * Сервис для аутентификации и регистрации пользователей.
 * <p>
 * Реализует бизнес-логику по созданию новых пользователей и выдаче JWT-токенов
 * при успешной авторизации.
 * Использует {@link UserRepository}, {@link RoleRepository}, {@link PasswordEncoder},
 * {@link JwtTokenProvider} и {@link AuthenticationManager}.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Проверяет уникальность email и имени пользователя,
     * хэширует пароль и присваивает роль {@code ROLE_USER}.
     *
     * @param user объект {@link User}, содержащий данные для регистрации
     * @return сообщение об успешной регистрации
     */
    public String register(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        var userRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
        user.setRole(userRole);

        userRepository.save(user);

        return "User registered successfully with role ROLE_USER";
    }

    /**
     * Выполняет аутентификацию пользователя и возвращает JWT-токен при успешном входе.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return сгенерированный JWT-токен для дальнейшей авторизации
     */
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        return jwtTokenProvider.generateToken(authentication);
    }
}
package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.dto.RegisterRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bankcards.exception.RoleNotFoundException;

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
     * @param userDto объект {@link User}, содержащий данные для регистрации
     * @return сообщение об успешной регистрации
     */
    public String register(RegisterRequestDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        var user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        var userRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new RoleNotFoundException(ROLE_USER.name()));
        user.setRole(userRole);

        userRepository.save(user);

        return "Пользователь успешно зарегестрирован с ролью ROLE_USER";
    }

    /**
     * Выполняет аутентификацию пользователя на основе данных из DTO {@link LoginRequestDto}
     * и возвращает JWT-токен при успешном входе.
     *
     * @param userDto объект {@link LoginRequestDto}, содержащий имя пользователя и пароль
     * @return сгенерированный JWT-токен для дальнейшей авторизации
     */
    public String login(LoginRequestDto userDto) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        );
        return jwtTokenProvider.generateToken(authentication);
    }
}
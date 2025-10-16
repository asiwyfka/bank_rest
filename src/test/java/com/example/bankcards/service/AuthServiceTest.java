package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.dto.RegisterRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.example.bankcards.entity.RoleName.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        var dto = new RegisterRequestDto();
        dto.setUsername("test");
        dto.setEmail("test@test.com");
        dto.setPassword("password");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());

        var role = new Role();
        role.setName(ROLE_USER);
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(role));

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        var result = authService.register(dto);

        assertEquals("Пользователь успешно зарегестрирован с ролью ROLE_USER", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailExists_shouldThrow() {
        var dto = new RegisterRequestDto();
        dto.setEmail("exists@test.com");
        dto.setUsername("user");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
    }

    @Test
    void register_usernameExists_shouldThrow() {
        var dto = new RegisterRequestDto();
        dto.setEmail("new@test.com");
        dto.setUsername("exists");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
    }

    @Test
    void register_roleNotFound_shouldThrow() {
        var dto = new RegisterRequestDto();
        dto.setEmail("new@test.com");
        dto.setUsername("new");
        dto.setPassword("pass");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> authService.register(dto));
    }

    @Test
    void login_success() {
        var dto = new LoginRequestDto();
        dto.setUsername("user");
        dto.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("token123");

        var token = authService.login(dto);

        assertEquals("token123", token);
    }
}

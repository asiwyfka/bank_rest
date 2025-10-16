package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserUpdateRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.RoleName;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        role = new Role();
        role.setId(1L);
        role.setName(RoleName.ROLE_USER);
    }

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserResponseDto> users = userService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserResponseDto dto = userService.getUserById(1L);
        assertEquals("testuser", dto.getUsername());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    void createUser_withRole() {
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("newuser");
        dto.setEmail("new@test.com");
        dto.setPassword("pass");
        dto.setRoleId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserResponseDto response = userService.createUser(dto);

        assertEquals("newuser", response.getUsername());
        assertEquals("ROLE_USER", response.getRoleName());
    }

    @Test
    void updateUser_notFound() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(2L, dto));
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(2L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(2L));
    }
}

package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для управления пользователями системы.
 * <p>
 * Предоставляет операции CRUD над сущностью {@link User}, включая создание, обновление,
 * получение и удаление пользователей. При создании и обновлении пароли кодируются с помощью
 * {@link PasswordEncoder} перед сохранением в базу данных.
 * <p>
 * Использует {@link UserRepository} для взаимодействия с уровнем хранения данных.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Возвращает список всех пользователей системы.
     *
     * @return список всех пользователей
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return объект {@link User}, если найден
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Создает нового пользователя.
     * <p>
     * Перед сохранением выполняется шифрование пароля с помощью {@link PasswordEncoder}.
     *
     * @param user объект {@link User}, содержащий данные нового пользователя
     * @return созданный пользователь с присвоенным идентификатором
     */
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Обновляет данные существующего пользователя.
     * <p>
     * Пароль также повторно шифруется перед сохранением.
     *
     * @param user объект {@link User} с обновленными данными
     * @return обновленный пользователь
     */
    public User updateUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
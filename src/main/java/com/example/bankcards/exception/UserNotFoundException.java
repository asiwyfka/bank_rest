package com.example.bankcards.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Пользователь с идентификатором = " + id + " не найден");
    }

    public UserNotFoundException(String username) {
        super("Пользователь с username = '" + username + "' не найден");
    }
}

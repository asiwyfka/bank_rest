package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("Карта с идентификатором = " + id + " у пользователя не найдена");
    }
}

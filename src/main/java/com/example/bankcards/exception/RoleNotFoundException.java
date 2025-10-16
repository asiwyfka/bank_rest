package com.example.bankcards.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Long id) { super("Роль с идентификатором = " + id + " не найдена"); }

    public RoleNotFoundException(String name) { super("Роль " + name + " не найдена"); }
}
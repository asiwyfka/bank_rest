package com.example.bankcards;

import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

public class PasswordHasher {
    public static void main(String[] args) {
        var key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}
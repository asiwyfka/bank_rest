package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/user/cards")
@Tag(name = "USER_CARD", description = "Функционал работы пользователей с картами")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class UserCardController {

    private final CardService cardService;

    @Operation(summary = "Получения всех своих карт пользователя")
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getUserCards(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.getUserCards(auth.getName(), PageRequest.of(page, size)));
    }

    @Operation(summary = "Запрос на блокировку своей карты")
    @PostMapping("/{id}/requestCardBlock")
    public ResponseEntity<?> requestCardBlock(@PathVariable Long id, Authentication auth) {
        cardService.requestCardBlock(id, auth.getName());
        return ResponseEntity.ok("Запрос на блокировку карты отправлен");
    }

    @Operation(summary = "Перевод денежных средств с одной своей карты на другую свою карту")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            Authentication auth,
            @RequestParam Long fromCardId,
            @RequestParam Long toCardId,
            @RequestParam BigDecimal amount) {
        cardService.transfer(fromCardId, toCardId, amount, auth.getName());
        return ResponseEntity.ok("Перевод денежных средств успешно завершён");
    }

    @Operation(summary = "Получения баланса своей карты")
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(cardService.getCardBalance(id, auth.getName()));
    }
}
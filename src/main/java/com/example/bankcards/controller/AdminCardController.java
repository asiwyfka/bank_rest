package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/cards")
@Tag(name = "ADMIN_CARD", description = "Функционал работы с картами для администратора")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminCardController {
    private final CardService cardService;

    @Operation(summary = "Получение всех карт")
    @GetMapping
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    @Operation(summary = "Получение карты по id")
    @GetMapping("/{id}")
    public Card getCard(@PathVariable Long id) {
        return cardService.getCardById(id);
    }

    @Operation(summary = "Создание карты")
    @PostMapping
    public Card createCard(@RequestBody Card card) {
        return cardService.createCard(card);
    }

    @Operation(summary = "Обновление карты по id")
    @PatchMapping("/{id}")
    public Card updateCard(@PathVariable Long id, @RequestBody Card card) {
        card.setId(id);
        return cardService.updateCard(card);
    }

    @Operation(summary = "Удаление карты по id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Блокировка карты по id")
    @PatchMapping("/{id}/block")
    public Card blockCard(@PathVariable Long id) {
        return cardService.blockCard(id);
    }

    @Operation(summary = "Активация карты по id")
    @PatchMapping("/{id}/activate")
    public Card activateCard(@PathVariable Long id) {
        return cardService.activateCard(id);
    }
}
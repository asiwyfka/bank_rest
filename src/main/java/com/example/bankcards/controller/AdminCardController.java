package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final CardService cardService;

    @GetMapping
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    @GetMapping("/{id}")
    public Card getCard(@PathVariable Long id) {
        return cardService.getCardById(id);
    }

    @PostMapping
    public Card createCard(@RequestBody Card card) {
        return cardService.createCard(card);
    }

    @PatchMapping("/{id}")
    public Card updateCard(@PathVariable Long id, @RequestBody Card card) {
        card.setId(id);
        return cardService.updateCard(card);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/block")
    public Card blockCard(@PathVariable Long id) {
        return cardService.blockCard(id);
    }

    @PatchMapping("/{id}/activate")
    public Card activateCard(@PathVariable Long id) {
        return cardService.activateCard(id);
    }
}
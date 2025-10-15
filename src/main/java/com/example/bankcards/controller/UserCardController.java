package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserCardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<Card>> getUserCards(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.getUserCards(auth.getName(), PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/requestCardBlock")
    public ResponseEntity<?> requestCardBlock(@PathVariable Long id, Authentication auth) {
        cardService.requestCardBlock(id, auth.getName());
        return ResponseEntity.ok("Сard blocking request sent");
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            Authentication auth,
            @RequestParam Long fromCardId,
            @RequestParam Long toCardId,
            @RequestParam BigDecimal amount) {
        cardService.transfer(fromCardId, toCardId, amount, auth.getName());
        return ResponseEntity.ok("Transfer completed successfully");
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(cardService.getCardBalance(id, auth.getName()));
    }
}
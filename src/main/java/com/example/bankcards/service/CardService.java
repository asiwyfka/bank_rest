package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.example.bankcards.entity.CardStatus.ACTIVE;
import static com.example.bankcards.entity.CardStatus.BLOCKED;
import static com.example.bankcards.entity.CardStatus.BLOCK_REQUESTED;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Просмотр своих карт с пагинацией
     */
    public Page<Card> getUserCards(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardRepository.findByOwner(user, pageable);
    }

    /**
     * Запрос на блокировку карты (меняем статус)
     */
    @Transactional
    public void requestCardBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // Проверяем, что карта принадлежит пользователю
        if (!card.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("You can only request block for your own cards");
        }

        // Запрещаем повторный запрос, если уже заблокирована или запрос уже есть
        if (card.getStatus() == BLOCKED || card.getStatus() == BLOCK_REQUESTED) {
            throw new RuntimeException("Card is already blocked or block requested");
        }

        card.setStatus(BLOCK_REQUESTED);
        cardRepository.save(card);
    }

    /**
     * Перевод между своими картами
     */
    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount, String username) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Card from = getOwnedCard(fromCardId, username);
        Card to = getOwnedCard(toCardId, username);

        if (from.getStatus() == BLOCKED || to.getStatus() == BLOCKED)
            throw new IllegalStateException("One of the cards is blocked");

        if (from.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    /**
     * Получение баланса конкретной карты
     */
    public BigDecimal getCardBalance(Long cardId, String username) {
        return getOwnedCard(cardId, username).getBalance();
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public Card createCard(Card card) {
        card.setStatus(ACTIVE); // по умолчанию активна
        return cardRepository.save(card);
    }

    public Card updateCard(Card card) {
        return cardRepository.save(card);
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public Card blockCard(Long id) {
        Card card = getCardById(id);
        card.setStatus(BLOCKED);
        return cardRepository.save(card);
    }

    public Card activateCard(Long id) {
        Card card = getCardById(id);
        card.setStatus(ACTIVE);
        return cardRepository.save(card);
    }

    private Card getOwnedCard(Long cardId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cardRepository.findById(cardId)
                .filter(card -> card.getOwner().equals(user))
                .orElseThrow(() -> new RuntimeException("Card not found or does not belong to user"));
    }

}
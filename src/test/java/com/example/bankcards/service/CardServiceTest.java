package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567812345678");
        card.setOwner(user);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setStatus(CardStatus.ACTIVE);
    }

    @Test
    void getUserCards_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cardRepository.findByOwner(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        Page<CardResponseDto> result = cardService.getUserCards("testuser", Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUserCards_userNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> cardService.getUserCards("unknown", Pageable.unpaged()));
    }

    @Test
    void requestCardBlock_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        cardService.requestCardBlock(1L, "testuser");
        assertEquals(CardStatus.BLOCK_REQUESTED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void transfer_success() {
        Card card2 = new Card();
        card2.setId(2L);
        card2.setOwner(user);
        card2.setBalance(BigDecimal.valueOf(500));
        card2.setStatus(CardStatus.ACTIVE);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        cardService.transfer(1L, 2L, BigDecimal.valueOf(200), "testuser");

        assertEquals(BigDecimal.valueOf(800), card.getBalance());
        assertEquals(BigDecimal.valueOf(700), card2.getBalance());
    }

    @Test
    void createCard_success() {
        CardRequestDto dto = new CardRequestDto();
        dto.setCardNumber("8765432187654321");
        dto.setOwnerId(user.getId());
        dto.setBalance(BigDecimal.valueOf(300));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArguments()[0]);

        CardResponseDto response = cardService.createCard(dto);

        assertEquals("**** **** **** 4321", response.getMaskedNumber());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
    }

    @Test
    void deleteCard_notFound() {
        when(cardRepository.existsById(2L)).thenReturn(false);
        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(2L));
    }
}

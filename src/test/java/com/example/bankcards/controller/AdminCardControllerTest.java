package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.bankcards.entity.CardStatus.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminCardController adminCardController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminCardController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getAllCards_success() throws Exception {
        var card = new CardResponseDto();
        card.setId(1L);
        card.setMaskedNumber("**** **** **** 1234");
        card.setOwnerId(1L);
        card.setStatus(ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setExpiryDate(LocalDate.of(2028, 10, 15));

        when(cardService.getAllCards()).thenReturn(List.of(card));

        mockMvc.perform(get("/admin/cards")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 1234"));
    }

    @Test
    void createCard_success() throws Exception {
        var uniqueCardNumber = "1234567887654321";

        var request = new CardRequestDto();
        request.setCardNumber(uniqueCardNumber);
        request.setOwnerId(1L);
        request.setBalance(BigDecimal.valueOf(500));
        request.setExpiryDate(LocalDate.of(2028, 10, 15));

        var response = new CardResponseDto();
        response.setId(1L);
        response.setMaskedNumber("**** **** **** 4321");
        response.setOwnerId(1L);
        response.setStatus(ACTIVE);
        response.setBalance(BigDecimal.valueOf(500));
        response.setExpiryDate(LocalDate.of(2028, 10, 15));

        when(cardService.createCard(any(CardRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/cards")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    void deleteCard_success() throws Exception {
        mockMvc.perform(delete("/admin/cards/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1L);
    }
}

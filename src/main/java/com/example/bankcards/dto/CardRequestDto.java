package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardRequestDto {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance = BigDecimal.ZERO;
}

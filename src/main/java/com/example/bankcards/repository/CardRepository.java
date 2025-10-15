package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByOwner(User owner, Pageable pageable);
}
package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
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

/**
 * Сервис для работы с банковскими картами пользователей.
 * <p>
 * Содержит операции по созданию, управлению статусом и переводам между картами.
 * Использует {@link CardRepository} и {@link UserRepository}.
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Возвращает список карт текущего пользователя с поддержкой пагинации.
     *
     * @param username имя пользователя, чьи карты необходимо получить
     * @param pageable параметры пагинации (номер страницы, размер, сортировка)
     * @return страница карт пользователя
     */
    public Page<CardResponseDto> getUserCards(String username, Pageable pageable) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        return cardRepository.findByOwner(user, pageable)
                .map(this::toDto);
    }

    /**
     * Отправляет запрос на блокировку карты.
     * Меняет статус карты на BLOCK_REQUESTED.
     *
     * @param cardId   идентификатор карты
     * @param username имя пользователя, делающего запрос
     * @throws RuntimeException если карта не найдена, принадлежит другому пользователю,
     *                          либо уже заблокирована или запрошена блокировка
     */
    @Transactional
    public void requestCardBlock(Long cardId, String username) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Вы можете запросить блокировку только своей карты");
        }

        if (card.getStatus() == BLOCKED || card.getStatus() == BLOCK_REQUESTED) {
            throw new RuntimeException("Карта уже заблокирована или блокировка запрошена ранее");
        }

        card.setStatus(BLOCK_REQUESTED);
        cardRepository.save(card);
    }

    /**
     * Выполняет перевод средств между своими картами.
     *
     * @param fromCardId идентификатор карты-отправителя
     * @param toCardId   идентификатор карты-получателя
     * @param amount     сумма перевода (должна быть положительной)
     * @param username   имя владельца карт
     */
    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount, String username) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");

        var from = getOwnedCard(fromCardId, username);
        var to = getOwnedCard(toCardId, username);

        if (from.getStatus() == BLOCKED || to.getStatus() == BLOCKED)
            throw new IllegalStateException("Одна из карт заблокирована");

        if (from.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Недостаточно средств на карте отправителя");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    /**
     * Возвращает баланс конкретной карты, принадлежащей пользователю.
     *
     * @param cardId   идентификатор карты
     * @param username имя владельца карты
     * @return текущий баланс карты
     */
    public BigDecimal getCardBalance(Long cardId, String username) {
        return getOwnedCard(cardId, username).getBalance();
    }

    /**
     * Возвращает список всех карт в системе.
     *
     * @return список всех карт
     */
    public List<CardResponseDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Возвращает карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return найденная карта
     */
    public CardResponseDto getCardById(Long id) {
        return toDto(cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id)));
    }

    /**
     * Создаёт новую карту и устанавливает ей статус CardStatus.ACTIVE.
     *
     * @param cardDto данные карты для создания
     * @return созданная карта
     */
    public CardResponseDto createCard(CardRequestDto cardDto) {
        var owner = userRepository.findById(cardDto.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException(cardDto.getOwnerId()));

        var card = new Card();
        card.setCardNumber(cardDto.getCardNumber());
        card.setOwner(owner);
        card.setExpiryDate(cardDto.getExpiryDate());
        card.setStatus(ACTIVE);
        card.setBalance(cardDto.getBalance());

        return toDto(cardRepository.save(card));
    }

    /**
     * Обновляет данные существующей карты.
     *
     * @param id  идентификатор карты
     * @param cardDto обновлённые данные
     * @return обновлённая карта
     */
    public CardResponseDto updateCard(Long id, CardRequestDto cardDto) {
        var card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (cardDto.getCardNumber() != null) card.setCardNumber(cardDto.getCardNumber());
        if (cardDto.getExpiryDate() != null) card.setExpiryDate(cardDto.getExpiryDate());
        if (cardDto.getBalance() != null) card.setBalance(cardDto.getBalance());

        return toDto(cardRepository.save(card));
    }

    /**
     * Блокирует карту (для администратора).
     *
     * @param id идентификатор карты
     * @return обновлённая карта со статусом {@code BLOCKED}
     */
    public CardResponseDto blockCard(Long id) {
        var card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(BLOCKED);
        return toDto(cardRepository.save(card));
    }

    /**
     * Активирует карту (для администратора).
     *
     * @param id идентификатор карты
     * @return обновлённая карта со статусом {@code ACTIVE}
     */
    public CardResponseDto activateCard(Long id) {
        var card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(ACTIVE);
        return toDto(cardRepository.save(card));
    }

    /**
     * Удаляет карту по идентификатору.
     *
     * @param id идентификатор карты
     */
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id))
            throw new CardNotFoundException(id);
        cardRepository.deleteById(id);
    }

    /**
     * Возвращает карту, принадлежащую указанному пользователю.
     *
     * @param cardId   идентификатор карты
     * @param username имя владельца карты
     * @return карта, принадлежащая пользователю
     */
    private Card getOwnedCard(Long cardId, String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        return cardRepository.findById(cardId)
                .filter(card -> card.getOwner().equals(user))
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    /**
     * Преобразует сущность {@link Card} в DTO {@link CardResponseDto}.
     *
     * @param card объект карты
     * @return DTO карты
     */
    private CardResponseDto toDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                card.getMaskedNumber(),
                card.getOwner().getId(),
                card.getStatus(),
                card.getBalance(),
                card.getExpiryDate(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
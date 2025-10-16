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
     * Возвращает список карт текущего пользователя с пагинацией.
     *
     * @param username имя пользователя, чьи карты необходимо получить
     * @param pageable параметры пагинации (номер страницы, размер, сортировка)
     * @return страница карт пользователя
     */
    public Page<Card> getUserCards(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardRepository.findByOwner(user, pageable);
    }

    /**
     * Отправляет запрос на блокировку карты. Меняет статус карты на {@code BLOCK_REQUESTED}.
     *
     * @param cardId   идентификатор карты
     * @param username имя пользователя, делающего запрос
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
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    /**
     * Возвращает карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return найденная карта
     */
    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    /**
     * Создает новую карту с активным статусом по умолчанию.
     *
     * @param card объект карты для сохранения
     * @return созданная карта с присвоенным идентификатором
     */
    public Card createCard(Card card) {
        card.setStatus(ACTIVE); // по умолчанию активна
        return cardRepository.save(card);
    }

    /**
     * Обновляет данные существующей карты.
     *
     * @param card обновленный объект карты
     * @return сохраненная карта
     */
    public Card updateCard(Card card) {
        return cardRepository.save(card);
    }

    /**
     * Удаляет карту по её идентификатору.
     *
     * @param id идентификатор карты
     */
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    /**
     * Блокирует карту, устанавливая статус {@code BLOCKED}.
     *
     * @param id идентификатор карты
     * @return обновленная карта
     */
    public Card blockCard(Long id) {
        Card card = getCardById(id);
        card.setStatus(BLOCKED);
        return cardRepository.save(card);
    }

    /**
     * Активирует карту, устанавливая статус {@code ACTIVE}.
     *
     * @param id идентификатор карты
     * @return обновленная карта
     */
    public Card activateCard(Long id) {
        Card card = getCardById(id);
        card.setStatus(ACTIVE);
        return cardRepository.save(card);
    }

    /**
     * Вспомогательный метод: возвращает карту, принадлежащую указанному пользователю.
     *
     * @param cardId   идентификатор карты
     * @param username имя владельца карты
     * @return карта, принадлежащая пользователю
     */
    private Card getOwnedCard(Long cardId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cardRepository.findById(cardId)
                .filter(card -> card.getOwner().equals(user))
                .orElseThrow(() -> new RuntimeException("Card not found or does not belong to user"));
    }

}
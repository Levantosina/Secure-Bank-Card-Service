package io.github.levantosina.bankcardmanagement.service.users;

import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.model.CardStatus;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.CardRepository;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import io.github.levantosina.bankcardmanagement.request.UserCardRegistrationRequest;
import io.github.levantosina.bankcardmanagement.service.AESService;
import io.github.levantosina.bankcardmanagement.service.ExtractUserIdFromContext;
import io.github.levantosina.bankcardmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    @InjectMocks
    private UserService underTest;
    @Mock
    private  CardRepository cardRepository;
    @Mock
    private  AESService aesService;
    @Mock
    private  ExtractUserIdFromContext extractUserIdFromContext;
    @Mock
    private  UserAdminRepository userAdminRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void findAllCardsByUserId() throws AccessDeniedException {
        Long mockUserId = 1L;
        int page = 0, size = 10;

        CardEntity card = new CardEntity();
        card.setCardHolderName("Test Name");
        card.setEncryptedCardNumber("encrypted123");
        card.setExpiryDate(YearMonth.of(2030, 1));
        card.setBalance(new BigDecimal("100.00"));
        card.setCardStatus(CardStatus.ACTIVE);

        Page<CardEntity> cardPage = new PageImpl<>(List.of(card));

        Mockito.when(extractUserIdFromContext.extractUserIdFromContext()).thenReturn(mockUserId);
        Mockito.when(cardRepository.findByUserUserId(mockUserId, PageRequest.of(page, size))).thenReturn(cardPage);
        Mockito.when(aesService.decrypt("encrypted123")).thenReturn("1234123412341234");
        Mockito.when(aesService.maskCardNumber("1234123412341234")).thenReturn("**** **** **** 1234");

        Page<CardDTO> result = underTest.findAllCardsByUserId(page, size,null);

        assertEquals(1, result.getContent().size());
        CardDTO dto = result.getContent().get(0);
        assertEquals("Test Name", dto.cardHolderName());
        assertEquals("**** **** **** 1234", dto.encryptedCardNumber());
        assertEquals(YearMonth.of(2030, 1), dto.expiryDate());
        assertEquals(new BigDecimal("100.00"), dto.balance());
        assertEquals(CardStatus.ACTIVE, dto.cardStatus());
    }

    @Test
    void createCard() throws AccessDeniedException {

        Long userId = 1L;
        UserCardRegistrationRequest request = new UserCardRegistrationRequest(
                "Test User",
                "1234123412341234",
                YearMonth.of(2026, 11),
                new BigDecimal("1000.00")

        );

        UserAdminEntity user = new UserAdminEntity();
        user.setUserId(userId);

        Mockito.when(extractUserIdFromContext.extractUserIdFromContext()).thenReturn(userId);
        Mockito.when(userAdminRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(aesService.encrypt("1234123412341234")).thenReturn("password123");


        underTest.createCard(request);


        ArgumentCaptor<CardEntity> cardCaptor = ArgumentCaptor.forClass(CardEntity.class);
        Mockito.verify(cardRepository).save(cardCaptor.capture());

        CardEntity savedCard = cardCaptor.getValue();
        assertEquals("Test User", savedCard.getCardHolderName());
        assertEquals("password123", savedCard.getEncryptedCardNumber());
        assertEquals(YearMonth.of(2026, 11), savedCard.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), savedCard.getBalance());
        assertEquals(CardStatus.ACTIVE, savedCard.getCardStatus());
        assertEquals(user, savedCard.getUser());
    }

    @Test
    void requestBlockCard() throws AccessDeniedException {
        Long cardId = 1L;
        Long userId = 1L;

        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.ACTIVE);
        card.setUser(new UserAdminEntity());
        card.getUser().setUserId(userId);

        Mockito.when(extractUserIdFromContext.extractUserIdFromContext()).thenReturn(userId);
        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        underTest.requestBlockCard(cardId);

        assertTrue(card.isBlockRequest());
        Mockito.verify(cardRepository).save(card);
    }


    @Test
    void transferBalance() {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        CardEntity fromCard = new CardEntity();
        fromCard.setBalance(new BigDecimal("200.00"));

        CardEntity toCard = new CardEntity();
        toCard.setBalance(new BigDecimal("200.00"));

        Mockito.when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        underTest.transferBalance(fromCardId, toCardId, amount);

        assertEquals(new BigDecimal("100.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("300.00"), toCard.getBalance());
        Mockito.verify(cardRepository).save(fromCard);
        Mockito.verify(cardRepository).save(toCard);
    }

    @Test
    void getBalance() throws AccessDeniedException {
        Long cardId = 1L;
        Long userId = 1L;

        CardEntity card = new CardEntity();
        card.setBalance(new BigDecimal("100.00"));
        card.setUser(new UserAdminEntity());
        card.getUser().setUserId(userId);

        Mockito.when(extractUserIdFromContext.extractUserIdFromContext()).thenReturn(userId);
        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal balance = underTest.getBalance(cardId);

        assertEquals(new BigDecimal("100.00"), balance);
    }
}
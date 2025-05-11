package io.github.levantosina.bankcardmanagement.service.users;

import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.dto.UserAdminDTO;
import io.github.levantosina.bankcardmanagement.mapper.UserAdminDTOMapper;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.model.CardStatus;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.CardRepository;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import io.github.levantosina.bankcardmanagement.request.AdminCardRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.UserCardRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.UserUpdateRequest;
import io.github.levantosina.bankcardmanagement.service.AESService;
import io.github.levantosina.bankcardmanagement.service.AdminService;
import io.github.levantosina.bankcardmanagement.service.ExtractUserIdFromContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class AdminServiceTest {

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserAdminDTOMapper userAdminDTOMapper;

    @Mock
    private AESService aesService;

    @InjectMocks
    private AdminService underTest;

    @Mock
    private ExtractUserIdFromContext extractUserIdFromContext;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllUsers() {
        UserAdminEntity user = new UserAdminEntity();
        user.setUserId(1L);
        user.setEmail("admin@gmail.com");
        user.setPassword("password123");

        UserAdminDTO expectedDTO = new UserAdminDTO("admin@gmail.com", "password123");

        Mockito.when(userAdminRepository.findAll()).thenReturn(List.of(user));
        Mockito.when(userAdminDTOMapper.apply(user)).thenReturn(expectedDTO);

        List<UserAdminDTO> result = underTest.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(expectedDTO, result.get(0));
    }

    @Test
    void findUserById() {
        Long userId = 1L;
        UserAdminEntity user = new UserAdminEntity();
        user.setUserId(userId);
        user.setEmail("admin@gmail.com");
        user.setPassword("password123");


        UserAdminDTO expectedDTO = new UserAdminDTO("admin@gmail.com", "password123");


        Mockito.when(userAdminRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userAdminDTOMapper.apply(user)).thenReturn(expectedDTO);

        UserAdminDTO result = underTest.findUserById(userId);
        assertEquals(expectedDTO, result);
    }


    @Test
    void testUpdateUserAdmin() {
        Long userId = 1L;
        UserAdminEntity existingUser = new UserAdminEntity();
        existingUser.setUserId(userId);
        existingUser.setEmail("admin@gmail.com");
        existingUser.setPassword("password123");

        UserUpdateRequest updateRequest = new UserUpdateRequest("newemail@gmail.com", "newPassword123");

        UserAdminEntity updatedUser = new UserAdminEntity();
        updatedUser.setUserId(userId);
        updatedUser.setEmail("newemail@gmail.com");
        updatedUser.setPassword("newPassword123");

        UserAdminDTO expectedDTO = new UserAdminDTO("newemail@gmail.com", "newPassword123");

        Mockito.when(userAdminRepository.findUserAdminByUserId(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userAdminRepository.save(existingUser)).thenReturn(updatedUser);
        Mockito.when(userAdminDTOMapper.apply(updatedUser)).thenReturn(expectedDTO);
        Mockito.when(passwordEncoder.encode(updateRequest.password())).thenReturn("newPassword123");


        UserAdminDTO result = underTest.updateUserAdmin(userId, updateRequest);
        assertEquals(expectedDTO, result);
    }
    @Test
    void deleteUser(){

        Long userId = 1L;
        Mockito.when(userAdminRepository.existsUserAdminByUserId(userId)).thenReturn(true);

        underTest.deleteUser(userId);
        Mockito.verify(userAdminRepository).deleteUserAdminByUserId(userId);
    }

    @Test
    void findAllCards_shouldReturnCardDTOs_whenCardsExist() {

        CardEntity card1 = new CardEntity();
        card1.setCardHolderName("Cardholder 1");
        card1.setEncryptedCardNumber("encryptedCard1");
        card1.setExpiryDate(YearMonth.of(2025, 12));
        card1.setBalance(new BigDecimal("50.00"));
        card1.setCardStatus(CardStatus.ACTIVE);

        CardEntity card2 = new CardEntity();
        card2.setCardHolderName("Cardholder 2");
        card2.setEncryptedCardNumber("encryptedCard2");
        card2.setExpiryDate(YearMonth.of(2027, 5));
        card2.setBalance(new BigDecimal("100.00"));
        card2.setCardStatus(CardStatus.BLOCKED);

        List<CardEntity> cardEntities = List.of(card1, card2);

        Mockito.when(cardRepository.findAll()).thenReturn(cardEntities);
        Mockito.when(aesService.decrypt("encryptedCard1")).thenReturn("1234123412341234");
        Mockito.when(aesService.decrypt("encryptedCard2")).thenReturn("5678567856785678");
        Mockito.when(aesService.maskCardNumber("1234123412341234")).thenReturn("**** **** **** 1234");
        Mockito.when(aesService.maskCardNumber("5678567856785678")).thenReturn("**** **** **** 5678");

        List<CardDTO> expected = List.of(
                new CardDTO("Cardholder 1", "**** **** **** 1234", YearMonth.of(2025, 12), new BigDecimal("50.00"), CardStatus.ACTIVE),
                new CardDTO("Cardholder 2", "**** **** **** 5678", YearMonth.of(2027, 5), new BigDecimal("100.00"), CardStatus.BLOCKED)
        );

        List<CardDTO> result = underTest.findAllCards();
        assertEquals(expected.size(), result.size());
        assertEquals(expected.get(0), result.get(0));
        assertEquals(expected.get(1), result.get(1));
    }
    @Test
    void findCardById() {

        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setCardHolderName("Cardholder Name");
        card.setEncryptedCardNumber("encryptedCard");
        card.setExpiryDate(YearMonth.of(2025, 12));
        card.setBalance(new BigDecimal("100.00"));
        card.setCardStatus(CardStatus.ACTIVE);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        Mockito.when(aesService.decrypt("encryptedCard")).thenReturn("1234123412341234");
        Mockito.when(aesService.maskCardNumber("1234123412341234")).thenReturn("**** **** **** 1234");

        CardDTO expected = new CardDTO("Cardholder Name", "**** **** **** 1234", YearMonth.of(2025, 12), new BigDecimal("100.00"), CardStatus.ACTIVE);

        CardDTO result = underTest.findCardById(cardId);
        assertEquals(expected, result);
    }

    @Test
    void createCard() throws AccessDeniedException {

        Long userId = 1L;
        AdminCardRegistrationRequest request = new AdminCardRegistrationRequest(
                "Test User",
                "1234123412341234",
                YearMonth.of(2026, 11),
                new BigDecimal("1000.00"),
                userId

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
    void blockCard(){
        Long cardId = 2L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.EXPIRED);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        Mockito.when(cardRepository.save(card)).thenReturn(card);

        CardEntity result = underTest.blockCard(cardId);
        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
    }
    @Test
    void activateCard() {
        Long cardId = 2L;
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.EXPIRED);
        card.setExpiryDate(YearMonth.of(2021, 1));
        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        YearMonth newExpiryDate = YearMonth.of(2026, 11);
        Mockito.when(cardRepository.save(Mockito.any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardEntity result = underTest.activateCard(cardId, newExpiryDate);

        assertNotNull(result, "Result should not be null");
        assertEquals(CardStatus.ACTIVE, result.getCardStatus());
        assertEquals(newExpiryDate, result.getExpiryDate());
    }
    @Test
    void deleteCard(){
        Long cardId = 2L;
        CardEntity card = new CardEntity();
        card.setCardId(cardId);

        Mockito.when(cardRepository.existsCardByCardId(cardId)).thenReturn(true);
        Mockito.doNothing().when(cardRepository).deleteCardByCardId(cardId);

        underTest.deleteCard(cardId);
        Mockito.verify(cardRepository, Mockito.times(1)).deleteCardByCardId(cardId);
    }
}




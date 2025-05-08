package io.github.levantosina.bankcardmanagement.controller;


import io.github.levantosina.bankcardmanagement.authentication.AuthenticationService;
import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.dto.UserAdminDTO;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.request.CardRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.UserRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.UserUpdateRequest;
import io.github.levantosina.bankcardmanagement.service.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/admin/card-management")
public class AdminController {

    private final AdminService adminService;
    private final AuthenticationService authenticationService;

///////////////////////// USER PART ////////////////////////////

    @GetMapping("/user/all")
    public ResponseEntity<List<UserAdminDTO>> getAllUsers() {
        List<UserAdminDTO> users = adminService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?>getUserById(@PathVariable("userId") long userId) {
        return ResponseEntity.ok(adminService.findUserById(userId));
    }

    @PostMapping("/user/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        authenticationService.createUser(request, true);
        return ResponseEntity.ok("Create user successfully");
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<?>  updateUser(@PathVariable("userId") Long userId,@RequestBody UserUpdateRequest userUpdateRequest){
       return ResponseEntity.ok(adminService.updateUserAdmin(userId,userUpdateRequest));
    }

    @DeleteMapping("/user/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {
       adminService.deleteUser(userId);
       return ResponseEntity.ok("User with id [%s] deleted successfully".formatted(userId));
    }
    ///////////////////////// USER PART ////////////////////////////


    ///////////////////////// CARD PART ////////////////////////////

    @GetMapping("/card/all")
    public ResponseEntity<?> getAllCards() {
        List<CardDTO> cards = adminService.findAllCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<?>getCardById(@PathVariable("cardId") long cardId) {
        return ResponseEntity.ok(adminService.findCardById(cardId));
    }

    @PostMapping("/card/create")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardRegistrationRequest cardRegistrationRequest) {
        adminService.createCard(cardRegistrationRequest);
        return ResponseEntity.ok("Card created successfully");
    }

    @PutMapping("/card/{cardId}/block")
    public ResponseEntity<CardEntity> blockCard(@PathVariable Long cardId) {
        CardEntity blockedCard = adminService.blockCard(cardId);
        return ResponseEntity.ok(blockedCard);
    }

    @PutMapping("/card/activate/{cardId}")
    public ResponseEntity<CardEntity> activateCard(@PathVariable Long cardId,
                                                   @RequestParam YearMonth newExpiryDate) {

        CardEntity updatedCard = adminService.activateCard(cardId, newExpiryDate);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/card/delete/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable("cardId") Long cardId) {
        adminService.deleteCard(cardId);
        return ResponseEntity.ok("Card with id [%s] deleted successfully".formatted(cardId));
    }

    ///////////////////////// CARD PART ////////////////////////////
}

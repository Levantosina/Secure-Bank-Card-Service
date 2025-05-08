package io.github.levantosina.bankcardmanagement.controller;

import io.github.levantosina.bankcardmanagement.request.CardTransferRequest;
import io.github.levantosina.bankcardmanagement.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/user/card-management")
public class UserController {

    private final UserService userService;


    @GetMapping("/user/allCards")
    public ResponseEntity<?> getUserById() throws AccessDeniedException {
        return ResponseEntity.ok(userService.findAllCardsByUserId());
    }
    @PutMapping("/block/{cardId}")
    public ResponseEntity<?> requestBlockCard(@PathVariable Long cardId) throws AccessDeniedException {
        userService.requestBlockCard(cardId);
        return ResponseEntity.ok("Card block request submitted");
    }
    @PostMapping("/cards/transfer")
    public ResponseEntity<String> transfer(@RequestBody CardTransferRequest request) {
        userService.transferBalance(request.fromCardId(), request.toCardId(), request.amount());
        return ResponseEntity.ok("Transfer request submitted");
    }
    @GetMapping("/balance/{cardId}")
    public ResponseEntity<?> getCardBalance(@PathVariable Long cardId) throws AccessDeniedException {
        return ResponseEntity.ok(userService.getBalance(cardId));
    }
}

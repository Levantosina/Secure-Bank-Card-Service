package io.github.levantosina.bankcardmanagement.controller;

import io.github.levantosina.bankcardmanagement.request.UserCardRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.CardTransferRequest;
import io.github.levantosina.bankcardmanagement.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/user/card-management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserController {

    private final UserService userService;


    @GetMapping("/user/allCards")
    public ResponseEntity<?> getUserById(  @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String cardHolderName)
            throws AccessDeniedException {
        return ResponseEntity.ok(userService.findAllCardsByUserId(page, size,cardHolderName));
    }

    @PostMapping("/card/create")
    public ResponseEntity<?> createCard(@RequestBody @Valid UserCardRegistrationRequest cardRegistrationRequest) throws AccessDeniedException {
        userService.createCard(cardRegistrationRequest);
        return ResponseEntity.ok("Card created successfully");
    }

    @PutMapping("/block/{cardId}")
    public ResponseEntity<?> requestBlockCard(@PathVariable Long cardId) throws AccessDeniedException {
        userService.requestBlockCard(cardId);
        return ResponseEntity.ok("Card block request submitted");
    }
    @PostMapping("/cards/transfer")
    public ResponseEntity<String> transfer(@Valid  @RequestBody CardTransferRequest request) {
        userService.transferBalance(request.fromCardId(), request.toCardId(), request.amount());
        return ResponseEntity.ok("Transfer request submitted");
    }
    @GetMapping("/balance/{cardId}")
    public ResponseEntity<?> getCardBalance(@PathVariable Long cardId) throws AccessDeniedException {
        return ResponseEntity.ok(userService.getBalance(cardId));
    }

}

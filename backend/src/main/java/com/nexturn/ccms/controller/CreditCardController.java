package com.nexturn.ccms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.nexturn.ccms.dto.CreditCardRequest;
import com.nexturn.ccms.dto.CreditCardResponse;
import com.nexturn.ccms.dto.CreditCardCvvResponse;
import com.nexturn.ccms.service.CustomerAccessService;
import com.nexturn.ccms.service.CreditCardService;

@RestController
@RequestMapping("/api/cards")
public class CreditCardController {

    private final CreditCardService creditCardService;
    private final CustomerAccessService customerAccessService;

    public CreditCardController(
            CreditCardService creditCardService,
            CustomerAccessService customerAccessService) {

        this.creditCardService = creditCardService;
        this.customerAccessService = customerAccessService;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> issueCard(
            @RequestBody CreditCardRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creditCardService.issueCard(request));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> getAllCards() {

        return ResponseEntity.ok(
                creditCardService.getAllCards());
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<CreditCardResponse> getCard(
            @PathVariable String cardNumber) {

        return ResponseEntity.ok(
                creditCardService.getCardByCardNumber(
                        cardNumber));
    }

    @GetMapping("/{cardNumber}/cvv")
    public ResponseEntity<CreditCardCvvResponse> getCardCvv(
            @PathVariable String cardNumber,
            Authentication authentication) {

        return ResponseEntity.ok(new CreditCardCvvResponse(
                creditCardService.getCardCvv(cardNumber, authentication)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditCardResponse>>
    getCardsByCustomer(
            @PathVariable Integer customerId,
            Authentication authentication) {

        customerAccessService.requireOwnCustomer(
                customerId, authentication);

        return ResponseEntity.ok(
                creditCardService.getCardsByCustomerId(
                        customerId));
    }

    @PatchMapping("/{cardNumber}/activate")
    public ResponseEntity<CreditCardResponse> activate(
            @PathVariable String cardNumber) {

        return ResponseEntity.ok(
                creditCardService.activateCard(cardNumber));
    }

    @PatchMapping("/{cardNumber}/block")
    public ResponseEntity<CreditCardResponse> block(
            @PathVariable String cardNumber) {

        return ResponseEntity.ok(
                creditCardService.blockCard(cardNumber));
    }

    @PatchMapping("/{cardNumber}/unblock")
    public ResponseEntity<CreditCardResponse> unblock(
            @PathVariable String cardNumber) {

        return ResponseEntity.ok(
                creditCardService.unblockCard(cardNumber));
    }

    @PatchMapping("/{cardNumber}/close")
    public ResponseEntity<CreditCardResponse> close(
            @PathVariable String cardNumber) {

        return ResponseEntity.ok(
                creditCardService.closeCard(cardNumber));
    }
}
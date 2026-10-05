package com.nexturn.ccms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexturn.ccms.dto.CardTypeRequest;
import com.nexturn.ccms.dto.CardTypeResponse;
import com.nexturn.ccms.service.CardTypeService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/card-types")
public class CardTypeController {

    private final CardTypeService cardTypeService;

    public CardTypeController(
            CardTypeService cardTypeService) {

        this.cardTypeService = cardTypeService;
    }

    @PostMapping
    public ResponseEntity<CardTypeResponse> create(
            @RequestBody CardTypeRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardTypeService.createCardType(request));
    }

    @GetMapping
    public ResponseEntity<List<CardTypeResponse>> getAll() {

        return ResponseEntity.ok(
                cardTypeService.getAllCardTypes());
    }

    @GetMapping("/{cardTypeId}")
    public ResponseEntity<CardTypeResponse> getById(
            @PathVariable Integer cardTypeId) {

        return ResponseEntity.ok(
                cardTypeService.getCardTypeById(cardTypeId));
    }

    @PutMapping("/{cardTypeId}")
    public ResponseEntity<CardTypeResponse> update(
            @PathVariable Integer cardTypeId,
            @RequestBody CardTypeRequest request) {

        return ResponseEntity.ok(
                cardTypeService.updateCardType(
                        cardTypeId, request));
    }

    @DeleteMapping("/{cardTypeId}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Integer cardTypeId) {

        cardTypeService.deleteCardType(cardTypeId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Card type deleted successfully"
                )
        );
    }
}
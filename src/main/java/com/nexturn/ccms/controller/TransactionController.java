package com.nexturn.ccms.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexturn.ccms.dto.TransactionRequest;
import com.nexturn.ccms.dto.TransactionResponse;
import com.nexturn.ccms.dto.TransactionSummaryResponse;
import com.nexturn.ccms.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	@Autowired
	TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {

        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<TransactionResponse>(response, HttpStatus.CREATED);   
    }
    
    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCardNumber(@PathVariable String cardNumber) {
    	
        List<TransactionResponse> response = transactionService.getTransactionsByCardNumber(cardNumber);
        return new ResponseEntity<List<TransactionResponse>>(response, HttpStatus.OK);
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable UUID transactionId) {

        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return new ResponseEntity<TransactionResponse>(response, HttpStatus.OK);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCustomerId(@PathVariable Long customerId) {

        List<TransactionResponse> response = transactionService.getTransactionsByCustomerId(customerId);
        return new ResponseEntity<List<TransactionResponse>> (response, HttpStatus.OK);
    }
    
    @GetMapping("/card/{cardNumber}/filter")
    public ResponseEntity<List<TransactionResponse>> filterTransactions(@PathVariable String cardNumber,
            @RequestParam(required = false) String type, @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        List<TransactionResponse> response = transactionService.filterTransactions(cardNumber, type, fromDate, toDate, minAmount, maxAmount);

        return new ResponseEntity<List<TransactionResponse>>(response, HttpStatus.OK);
    }
    
    @GetMapping("/card/{cardNumber}/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(@PathVariable String cardNumber) {

        TransactionSummaryResponse response = transactionService.getTransactionSummary(cardNumber);
        return new ResponseEntity<TransactionSummaryResponse>(response, HttpStatus.OK);
    }
    
}
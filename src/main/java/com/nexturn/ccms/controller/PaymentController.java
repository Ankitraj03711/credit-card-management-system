package com.nexturn.ccms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexturn.ccms.dto.PaymentRequest;
import com.nexturn.ccms.dto.PaymentResponse;
import com.nexturn.ccms.dto.PaymentSummaryResponse;
import com.nexturn.ccms.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<PaymentResponse>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(@PathVariable String paymentReference) {

        PaymentResponse response = paymentService.getPaymentByReference(paymentReference);
        return new ResponseEntity<PaymentResponse>(response, HttpStatus.OK);
    }
    
    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCardNumber(@PathVariable String cardNumber) {

        List<PaymentResponse> response = paymentService.getPaymentsByCardNumber(cardNumber);
        return new ResponseEntity<List<PaymentResponse>>(response, HttpStatus.OK);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(@PathVariable Long customerId) {

        List<PaymentResponse> response = paymentService.getPaymentsByCustomerId(customerId);
        return new ResponseEntity<List<PaymentResponse>>(response, HttpStatus.OK);
    }
    
    @GetMapping("/card/{cardNumber}/filter")
    public ResponseEntity<List<PaymentResponse>> filterPayments(@PathVariable String cardNumber, @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status, @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        List<PaymentResponse> response = paymentService.filterPayments(cardNumber, mode, status, fromDate, toDate, 
        		minAmount, maxAmount);
        return new ResponseEntity<List<PaymentResponse>>(response, HttpStatus.OK);
    }
    
    @GetMapping("/card/{cardNumber}/summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentSummary(@PathVariable String cardNumber) {

        PaymentSummaryResponse response = paymentService.getPaymentSummary(cardNumber);
        return new ResponseEntity<PaymentSummaryResponse>(response, HttpStatus.OK);
    }
}
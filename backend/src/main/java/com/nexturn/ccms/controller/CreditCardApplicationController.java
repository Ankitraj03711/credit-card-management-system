package com.nexturn.ccms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexturn.ccms.dto.CreditCardApplicationRequest;
import com.nexturn.ccms.dto.CreditCardApplicationResponse;
import com.nexturn.ccms.dto.RejectCreditCardApplicationRequest;
import com.nexturn.ccms.service.CreditCardApplicationService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/card-applications")
public class CreditCardApplicationController {

    private final CreditCardApplicationService applicationService;

    public CreditCardApplicationController(
            CreditCardApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<CreditCardApplicationResponse> submit(
            @RequestBody CreditCardApplicationRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                applicationService.submitApplication(
                        authentication.getName(), request));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<CreditCardApplicationResponse>> mine(
            Authentication authentication) {
        return ResponseEntity.ok(
                applicationService.getApplicationsByCustomer(
                        authentication.getName()));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditCardApplicationResponse>> byCustomer(
            @PathVariable Integer customerId,
            Authentication authentication) {
        return ResponseEntity.ok(
                applicationService.getApplicationsByCustomerId(
                        authentication.getName(), customerId));
    }

    @GetMapping("/customer/application/{applicationId}")
    public ResponseEntity<CreditCardApplicationResponse> mineById(
            @PathVariable String applicationId,
            Authentication authentication) {
        return ResponseEntity.ok(
                applicationService.getCustomerApplicationById(
                        authentication.getName(), applicationId));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardApplicationResponse>> all() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<CreditCardApplicationResponse> byId(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
                applicationService.getApplicationById(applicationId));
    }

    @PutMapping("/{applicationId}/approve")
    public ResponseEntity<CreditCardApplicationResponse> approve(
            @PathVariable String applicationId,
            Authentication authentication) {
        return ResponseEntity.ok(
                applicationService.approveApplication(
                        applicationId, authentication.getName()));
    }

    @PutMapping("/{applicationId}/reject")
    public ResponseEntity<CreditCardApplicationResponse> reject(
            @PathVariable String applicationId,
            @RequestBody(required = false)
            RejectCreditCardApplicationRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                applicationService.rejectApplication(
                        applicationId,
                        authentication.getName(),
                        request == null ? null : request.reason()));
    }
}

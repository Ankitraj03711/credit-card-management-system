package com.nexturn.ccms.controller;

import com.nexturn.ccms.dto.CustomerRequest;
import com.nexturn.ccms.dto.CustomerResponse;
import com.nexturn.ccms.dto.CustomerStatusRequest;
import com.nexturn.ccms.dto.CustomerUpdateRequest;
import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.service.CustomerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(
            CustomerService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse>
    create(
            @RequestBody CustomerRequest request,
            Authentication authentication) {
        if (request != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_CUSTOMER"))
                && !authentication.getName().equalsIgnoreCase(request.getEmail())) {
            throw new AccessDeniedException(
                    "Customers can only create their own profile");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>>
    getAll() {

        return ResponseEntity.ok(
            service.getAll()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getCurrentCustomer(
            Authentication authentication) {
        return ResponseEntity.ok(service.getByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateCurrentCustomer(
            @RequestBody CustomerUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                service.updateByEmail(authentication.getName(), request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse>
    getById(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(
            service.getById(customerId)
        );
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse>
    update(
            @PathVariable Integer customerId,
            @RequestBody CustomerUpdateRequest request) {

        return ResponseEntity.ok(
            service.update(
                customerId,
                request
            )
        );
    }

    @PatchMapping("/{customerId}/status")
    public ResponseEntity<CustomerResponse>
    updateStatus(
            @PathVariable Integer customerId,
            @RequestBody CustomerStatusRequest request) {

        return ResponseEntity.ok(
            service.updateStatus(
                customerId,
                request
            )
        );
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<MessageResponse>
    delete(
            @PathVariable Integer customerId) {

        service.delete(customerId);

        return ResponseEntity.ok(
            new MessageResponse(
                "Customer deleted successfully"
            )
        );
    }
}
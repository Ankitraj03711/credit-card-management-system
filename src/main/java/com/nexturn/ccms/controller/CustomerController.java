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

import java.util.List;

@RestController
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
            @RequestBody CustomerRequest request) {

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
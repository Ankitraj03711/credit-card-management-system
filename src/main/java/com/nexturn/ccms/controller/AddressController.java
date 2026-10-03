package com.nexturn.ccms.controller;

import com.nexturn.ccms.dto.AddressRequest;
import com.nexturn.ccms.dto.AddressResponse;
import com.nexturn.ccms.dto.AddressUpdateRequest;
import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.service.AddressService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(
            AddressService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AddressResponse>
    create(
            @RequestBody AddressRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping(
        "/customer/{customerId}"
    )
    public ResponseEntity<List<AddressResponse>>
    getByCustomer(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(
            service.getByCustomerId(customerId)
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse>
    update(
            @PathVariable UUID addressId,
            @RequestBody AddressUpdateRequest request) {

        return ResponseEntity.ok(
            service.update(
                addressId,
                request
            )
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<MessageResponse>
    delete(
            @PathVariable UUID addressId) {

        service.delete(addressId);

        return ResponseEntity.ok(
            new MessageResponse(
                "Address deleted successfully"
            )
        );
    }
}
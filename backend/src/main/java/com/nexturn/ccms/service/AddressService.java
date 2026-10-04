package com.nexturn.ccms.service;

import com.nexturn.ccms.dto.AddressRequest;
import com.nexturn.ccms.dto.AddressResponse;
import com.nexturn.ccms.dto.AddressUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressResponse create(
            AddressRequest request);

    List<AddressResponse> getByCustomerId(
            Integer customerId);

    AddressResponse update(
            UUID addressId,
            AddressUpdateRequest request);

    void delete(UUID addressId);
}
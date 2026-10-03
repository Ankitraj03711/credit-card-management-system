package com.nexturn.ccms.service;

import com.nexturn.ccms.dto.CustomerRequest;
import com.nexturn.ccms.dto.CustomerResponse;
import com.nexturn.ccms.dto.CustomerStatusRequest;
import com.nexturn.ccms.dto.CustomerUpdateRequest;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(
            CustomerRequest request);

    List<CustomerResponse> getAll();

    CustomerResponse getById(
            Integer customerId);

    CustomerResponse update(
            Integer customerId,
            CustomerUpdateRequest request);

    CustomerResponse updateStatus(
            Integer customerId,
            CustomerStatusRequest request);

    void delete(Integer customerId);
}
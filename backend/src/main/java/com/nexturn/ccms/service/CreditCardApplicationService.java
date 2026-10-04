package com.nexturn.ccms.service;

import java.util.List;

import com.nexturn.ccms.dto.CreditCardApplicationRequest;
import com.nexturn.ccms.dto.CreditCardApplicationResponse;

public interface CreditCardApplicationService {

    CreditCardApplicationResponse submitApplication(
            String customerEmail,
            CreditCardApplicationRequest request);

    CreditCardApplicationResponse getApplicationById(String applicationId);

    CreditCardApplicationResponse getCustomerApplicationById(
            String customerEmail,
            String applicationId);

    List<CreditCardApplicationResponse> getApplicationsByCustomer(
            String customerEmail);

    List<CreditCardApplicationResponse> getApplicationsByCustomerId(
            String customerEmail,
            Integer customerId);

    List<CreditCardApplicationResponse> getAllApplications();

    CreditCardApplicationResponse approveApplication(
            String applicationId,
            String reviewerEmail);

    CreditCardApplicationResponse rejectApplication(
            String applicationId,
            String reviewerEmail,
            String reason);
}

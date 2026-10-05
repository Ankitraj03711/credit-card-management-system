package com.nexturn.ccms.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.repository.CustomerRepository;

@Service
public class CustomerAccessService {

    private final CustomerRepository customerRepository;

    public CustomerAccessService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void requireOwnCustomer(
            Number customerId,
            Authentication authentication) {
        if (hasStaffRole(authentication)) {
            return;
        }
        Integer ownedId = customerRepository.findByEmail(authentication.getName())
                .map(Customer::getCustomerId)
                .orElseThrow(() -> new AccessDeniedException(
                        "A customer profile is required"));
        if (customerId.longValue() != ownedId.longValue()) {
            throw new AccessDeniedException(
                    "Customers can only access their own account data");
        }
    }

    private static boolean hasStaffRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_CUSTOMER_SERVICE"));
    }
}

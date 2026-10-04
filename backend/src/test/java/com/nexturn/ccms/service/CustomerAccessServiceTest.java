package com.nexturn.ccms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.repository.CustomerRepository;

class CustomerAccessServiceTest {

    private final CustomerRepository customerRepository =
            mock(CustomerRepository.class);

    private final CustomerAccessService customerAccessService =
            new CustomerAccessService(customerRepository);

    @Test
    void permitsCustomerToAccessTheirOwnCustomerId() {
        Customer customer = new Customer();
        customer.setCustomerId(101);
        when(customerRepository.findByEmail("customer-a@example.com"))
                .thenReturn(Optional.of(customer));

        assertDoesNotThrow(() -> customerAccessService.requireOwnCustomer(
                101, customerAuthentication("customer-a@example.com")));
    }

    @Test
    void deniesCustomerAccessToAnotherCustomerId() {
        Customer customer = new Customer();
        customer.setCustomerId(101);
        when(customerRepository.findByEmail("customer-a@example.com"))
                .thenReturn(Optional.of(customer));

        assertThrows(AccessDeniedException.class,
                () -> customerAccessService.requireOwnCustomer(
                        202, customerAuthentication("customer-a@example.com")));
    }

    private static UsernamePasswordAuthenticationToken customerAuthentication(
            String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }
}

package com.nexturn.ccms.serviceimpl;

import com.nexturn.ccms.dto.CustomerRequest;
import com.nexturn.ccms.dto.CustomerResponse;
import com.nexturn.ccms.dto.CustomerStatusRequest;
import com.nexturn.ccms.dto.CustomerUpdateRequest;
import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.entity.UserLogin;
import com.nexturn.ccms.enums.CustomerStatus;
import com.nexturn.ccms.enums.UserRole;
import com.nexturn.ccms.exception.CustomerNotFoundException;
import com.nexturn.ccms.exception.DuplicateEmailException;
import com.nexturn.ccms.exception.UserNotFoundException;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.repository.UserLoginRepository;
import com.nexturn.ccms.service.CustomerService;
import com.nexturn.ccms.service.IdGeneratorService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerServiceImpl
        implements CustomerService {

    private static final String CUSTOMER_ID =
            "CUSTOMER_ID";

    private final CustomerRepository customerRepository;
    private final UserLoginRepository userLoginRepository;
    private final IdGeneratorService idGeneratorService;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            UserLoginRepository userLoginRepository,
            IdGeneratorService idGeneratorService) {

        this.customerRepository =
                customerRepository;

        this.userLoginRepository =
                userLoginRepository;

        this.idGeneratorService =
                idGeneratorService;
    }

    @Override
    @Transactional
    public CustomerResponse create(
            CustomerRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        validateCustomer(
            request.getName(),
            request.getPhone(),
            request.getAnnualIncome(),
            request.getEmail()
        );
        validateProfileAddress(request.getAddress());

        String email =
                request.getEmail().trim();

        if (customerRepository
                .existsByEmail(email)) {

            throw new DuplicateEmailException(
                "Customer email already exists: "
                + email
            );
        }

        UserLogin user =
            userLoginRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new UserNotFoundException(
                        "User not found. Register the user first."
                    ));
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customer accounts can have a customer profile");
        }

        Long nextId =
            idGeneratorService.getNextValue(
                CUSTOMER_ID
            );

        Customer customer =
                new Customer();

        customer.setCustomerId(
            Math.toIntExact(nextId)
        );

        customer.setName(
            request.getName().trim()
        );
        setStructuredName(customer, request.getName().trim());

        customer.setPhone(
            request.getPhone().trim()
        );

        customer.setAddress(
            request.getAddress() == null || request.getAddress().isBlank()
                ? null
                : request.getAddress().trim()
        );

        customer.setAnnualIncome(
            request.getAnnualIncome()
        );

        customer.setEmail(email);

        customer.setCustomerStatus(
            request.getCustomerStatus() != null
                ? request.getCustomerStatus()
                : CustomerStatus.ACTIVE
        );

        customer.setUserLogin(user);

        Customer saved =
                customerRepository.save(customer);

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {

        return customerRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(
            Integer customerId) {

        return convertToResponse(
            findCustomer(customerId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer profile not found for the signed-in user"));
        return convertToProfileResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(
            Integer customerId,
            CustomerUpdateRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        Customer customer =
                findCustomer(customerId);

        return updateCustomer(customer, request);
    }

    @Override
    @Transactional
    public CustomerResponse updateByEmail(
            String email,
            CustomerUpdateRequest request) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "Authenticated email is required"
            );
        }
        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer profile not found for the signed-in user"));

        return updateCustomer(customer, request);
    }

    private CustomerResponse updateCustomer(
            Customer customer,
            CustomerUpdateRequest request) {

        if (request.getName() == null ||
            request.getName().isBlank()) {

            throw new IllegalArgumentException(
                "Name is required"
            );
        }

        if (request.getPhone() == null ||
            request.getPhone().isBlank()) {

            throw new IllegalArgumentException(
                "Phone is required"
            );
        }

        if (request.getAnnualIncome() == null ||
            request.getAnnualIncome()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                "Annual income must be zero or greater"
            );
        }

        customer.setName(
            request.getName().trim()
        );
        setStructuredName(customer, request.getName().trim());

        customer.setPhone(
            request.getPhone().trim()
        );

        if (request.getAddress() != null) {
            validateProfileAddress(request.getAddress());
            customer.setAddress(
                request.getAddress().isBlank()
                    ? null
                    : request.getAddress().trim()
            );
        }

        customer.setAnnualIncome(
            request.getAnnualIncome()
        );

        return convertToResponse(
            customerRepository.save(customer)
        );
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(
            Integer customerId,
            CustomerStatusRequest request) {

        if (request == null ||
            request.getStatus() == null) {

            throw new IllegalArgumentException(
                "Status is required"
            );
        }

        Customer customer =
                findCustomer(customerId);

        customer.setCustomerStatus(
            request.getStatus()
        );

        return convertToResponse(
            customerRepository.save(customer)
        );
    }

    @Override
    @Transactional
    public void delete(Integer customerId) {

        Customer customer =
                findCustomer(customerId);

        customerRepository.delete(customer);
    }

    private Customer findCustomer(
            Integer customerId) {

        if (customerId == null) {

            throw new IllegalArgumentException(
                "Customer ID is required"
            );
        }

        return customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                    new CustomerNotFoundException(
                        "Customer not found: "
                        + customerId
                    ));
    }

    private void validateCustomer(
            String name,
            String phone,
            BigDecimal annualIncome,
            String email) {

        if (name == null || name.isBlank()) {

            throw new IllegalArgumentException(
                "Name is required"
            );
        }

        if (phone == null || phone.isBlank()) {

            throw new IllegalArgumentException(
                "Phone is required"
            );
        }

        if (annualIncome == null ||
            annualIncome.compareTo(
                BigDecimal.ZERO
            ) < 0) {

            throw new IllegalArgumentException(
                "Annual income must be zero or greater"
            );
        }

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                "Email is required"
            );
        }
    }

    private static void validateProfileAddress(String address) {
        if (address != null && address.length() > 500) {
            throw new IllegalArgumentException(
                "Address must be 500 characters or fewer"
            );
        }
    }

    private static void setStructuredName(
            Customer customer,
            String fullName) {

        String[] nameParts =
                fullName.trim().split("\\s+");

        customer.setFirstName(nameParts[0]);
        customer.setMiddleName(
            nameParts.length > 2
                ? String.join(
                    " ",
                    java.util.Arrays.copyOfRange(
                        nameParts,
                        1,
                        nameParts.length - 1
                    )
                )
                : null
        );
        customer.setLastName(
            nameParts.length > 1
                ? nameParts[nameParts.length - 1]
                : null
        );
    }

    private CustomerResponse convertToResponse(
            Customer customer) {

        return new CustomerResponse(
            customer.getCustomerId(),
            customer.getName(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getAnnualIncome(),
            customer.getEmail(),
            customer.getCustomerStatus()
        );
    }

    private CustomerResponse convertToProfileResponse(
            Customer customer) {

        CustomerResponse response = convertToResponse(customer);
        if (response.getAddress() == null || response.getAddress().isBlank()) {
            response.setAddress(customer.getAddresses().stream()
                    .findFirst()
                    .map(address -> String.join(", ",
                            address.getHomeNumber(),
                            address.getTown(),
                            address.getDistrict(),
                            address.getState(),
                            address.getPinCode(),
                            address.getCountry()))
                    .orElse(null));
        }
        return response;
    }
}
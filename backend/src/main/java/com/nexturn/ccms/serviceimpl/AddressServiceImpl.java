package com.nexturn.ccms.serviceimpl;

import com.nexturn.ccms.dto.AddressRequest;
import com.nexturn.ccms.dto.AddressResponse;
import com.nexturn.ccms.dto.AddressUpdateRequest;
import com.nexturn.ccms.entity.Address;
import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.exception.AddressNotFoundException;
import com.nexturn.ccms.exception.CustomerNotFoundException;
import com.nexturn.ccms.repository.AddressRepository;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.service.AddressService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressServiceImpl
        implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            CustomerRepository customerRepository) {

        this.addressRepository =
                addressRepository;

        this.customerRepository =
                customerRepository;
    }

    @Override
    @Transactional
    public AddressResponse create(
            AddressRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        validate(
            request.getHomeNumber(),
            request.getTown(),
            request.getPinCode(),
            request.getDistrict(),
            request.getState(),
            request.getCountry()
        );

        if (request.getCustomerId() == null) {

            throw new IllegalArgumentException(
                "Customer ID is required"
            );
        }

        Customer customer =
            customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                    new CustomerNotFoundException(
                        "Customer not found: "
                        + request.getCustomerId()
                    ));

        Address address =
                new Address();

        address.setHomeNumber(
            request.getHomeNumber().trim()
        );

        address.setTown(
            request.getTown().trim()
        );

        address.setPinCode(
            request.getPinCode().trim()
        );

        address.setDistrict(
            request.getDistrict().trim()
        );

        address.setState(
            request.getState().trim()
        );

        address.setCountry(
            request.getCountry().trim()
        );

        address.setCustomer(customer);

        Address saved =
                addressRepository.save(address);

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getByCustomerId(
            Integer customerId) {

        if (customerId == null) {

            throw new IllegalArgumentException(
                "Customer ID is required"
            );
        }

        if (!customerRepository.existsById(
                customerId)) {

            throw new CustomerNotFoundException(
                "Customer not found: "
                + customerId
            );
        }

        return addressRepository
                .findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse update(
            UUID addressId,
            AddressUpdateRequest request) {

        if (addressId == null) {

            throw new IllegalArgumentException(
                "Address ID is required"
            );
        }

        if (request == null) {

            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        Address address =
            addressRepository
                .findById(addressId)
                .orElseThrow(() ->
                    new AddressNotFoundException(
                        "Address not found: "
                        + addressId
                    ));

        validate(
            request.getHomeNumber(),
            request.getTown(),
            request.getPinCode(),
            request.getDistrict(),
            request.getState(),
            request.getCountry()
        );

        address.setHomeNumber(
            request.getHomeNumber().trim()
        );

        address.setTown(
            request.getTown().trim()
        );

        address.setPinCode(
            request.getPinCode().trim()
        );

        address.setDistrict(
            request.getDistrict().trim()
        );

        address.setState(
            request.getState().trim()
        );

        address.setCountry(
            request.getCountry().trim()
        );

        return convertToResponse(
            addressRepository.save(address)
        );
    }

    @Override
    @Transactional
    public void delete(UUID addressId) {

        if (addressId == null) {

            throw new IllegalArgumentException(
                "Address ID is required"
            );
        }

        Address address =
            addressRepository
                .findById(addressId)
                .orElseThrow(() ->
                    new AddressNotFoundException(
                        "Address not found: "
                        + addressId
                    ));

        addressRepository.delete(address);
    }

    private void validate(
            String homeNumber,
            String town,
            String pinCode,
            String district,
            String state,
            String country) {

        if (homeNumber == null ||
            homeNumber.isBlank()) {

            throw new IllegalArgumentException(
                "Home number is required"
            );
        }

        if (town == null || town.isBlank()) {

            throw new IllegalArgumentException(
                "Town is required"
            );
        }

        if (pinCode == null ||
            pinCode.isBlank()) {

            throw new IllegalArgumentException(
                "Pin code is required"
            );
        }

        if (district == null ||
            district.isBlank()) {

            throw new IllegalArgumentException(
                "District is required"
            );
        }

        if (state == null ||
            state.isBlank()) {

            throw new IllegalArgumentException(
                "State is required"
            );
        }

        if (country == null ||
            country.isBlank()) {

            throw new IllegalArgumentException(
                "Country is required"
            );
        }
    }

    private AddressResponse convertToResponse(Address address) {

    	AddressResponse response = new AddressResponse();

    	response.setAddressId(address.getAddressId());
    	response.setHomeNumber(address.getHomeNumber());
    	response.setTown(address.getTown());
    	response.setPinCode(address.getPinCode());
    	response.setDistrict(address.getDistrict());
    	response.setState(address.getState());
    	response.setCountry(address.getCountry());
    	response.setCustomerId(address.getCustomer().getCustomerId());

    	return response;
    }
}
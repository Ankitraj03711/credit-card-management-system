package com.nexturn.ccms.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexturn.ccms.dto.CreditCardApplicationRequest;
import com.nexturn.ccms.dto.CreditCardApplicationResponse;
import com.nexturn.ccms.dto.CreditCardRequest;
import com.nexturn.ccms.entity.CardType;
import com.nexturn.ccms.entity.CreditCardApplication;
import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.entity.UserLogin;
import com.nexturn.ccms.enums.CreditCardApplicationStatus;
import com.nexturn.ccms.enums.CustomerStatus;
import com.nexturn.ccms.exception.CardTypeNotFoundException;
import com.nexturn.ccms.exception.CustomerNotFoundException;
import com.nexturn.ccms.exception.CreditCardApplicationNotFoundException;
import com.nexturn.ccms.repository.CardTypeRepository;
import com.nexturn.ccms.repository.CreditCardApplicationRepository;
import com.nexturn.ccms.repository.CreditCardDetailsRepository;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.repository.UserLoginRepository;
import com.nexturn.ccms.service.CreditCardApplicationService;
import com.nexturn.ccms.service.CreditCardService;

@Service
public class CreditCardApplicationServiceImpl
        implements CreditCardApplicationService {

    private final CreditCardApplicationRepository applicationRepository;
    private final CustomerRepository customerRepository;
    private final CardTypeRepository cardTypeRepository;
    private final UserLoginRepository userLoginRepository;
    private final CreditCardDetailsRepository creditCardRepository;
    private final CreditCardService creditCardService;

    public CreditCardApplicationServiceImpl(
            CreditCardApplicationRepository applicationRepository,
            CustomerRepository customerRepository,
            CardTypeRepository cardTypeRepository,
            UserLoginRepository userLoginRepository,
            CreditCardDetailsRepository creditCardRepository,
            CreditCardService creditCardService) {
        this.applicationRepository = applicationRepository;
        this.customerRepository = customerRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.userLoginRepository = userLoginRepository;
        this.creditCardRepository = creditCardRepository;
        this.creditCardService = creditCardService;
    }

    @Override
    @Transactional
    public CreditCardApplicationResponse submitApplication(
            String customerEmail,
            CreditCardApplicationRequest request) {
        if (request == null || request.cardTypeId() == null) {
            throw new IllegalArgumentException("A card product is required");
        }
        if (request.requestedCreditLimit() == null
                || request.requestedCreditLimit().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Requested credit limit must be greater than zero");
        }
        BigDecimal requestedLimit = request.requestedCreditLimit().stripTrailingZeros();
        if (requestedLimit.scale() > 2
                || requestedLimit.precision() - requestedLimit.scale() > 10) {
            throw new IllegalArgumentException(
                    "Requested credit limit supports up to 10 integer and 2 decimal digits");
        }

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer profile not found for the signed-in user"));
        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active customers can apply for a credit card");
        }
        CardType cardType = cardTypeRepository.findById(request.cardTypeId())
                .orElseThrow(() -> new CardTypeNotFoundException(
                        "Card product not found: " + request.cardTypeId()));
        if (cardType.getNetwork() == null) {
            throw new IllegalArgumentException(
                    "This card product is not available for applications");
        }

        CreditCardApplication application = new CreditCardApplication();
        application.setCustomer(customer);
        application.setCardType(cardType);
        application.setRequestedCreditLimit(request.requestedCreditLimit());
        application.setStatus(CreditCardApplicationStatus.PENDING);
        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public CreditCardApplicationResponse getApplicationById(
            String applicationId) {
        return toResponse(findApplication(applicationId));
    }

    @Override
    @Transactional(readOnly = true)
    public CreditCardApplicationResponse getCustomerApplicationById(
            String customerEmail,
            String applicationId) {
        CreditCardApplication application = findApplication(applicationId);
        if (!application.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Customers can only view their own applications");
        }
        return toResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardApplicationResponse> getApplicationsByCustomer(
            String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer profile not found for the signed-in user"));
        return applicationRepository
                .findByCustomerCustomerIdOrderByApplicationDateDesc(
                        customer.getCustomerId())
                .stream()
                .map(CreditCardApplicationServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardApplicationResponse> getApplicationsByCustomerId(
            String customerEmail,
            Integer customerId) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .filter(found -> found.getCustomerId().equals(customerId))
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "Customers can only view their own applications"));
        return applicationRepository
                .findByCustomerCustomerIdOrderByApplicationDateDesc(
                        customer.getCustomerId())
                .stream()
                .map(CreditCardApplicationServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardApplicationResponse> getAllApplications() {
        return applicationRepository.findAllByOrderByApplicationDateDesc()
                .stream()
                .map(CreditCardApplicationServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CreditCardApplicationResponse approveApplication(
            String applicationId,
            String reviewerEmail) {
        CreditCardApplication application = findPendingApplication(applicationId);
        UserLogin reviewer = userLoginRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated reviewer account was not found"));

        application.setStatus(CreditCardApplicationStatus.APPROVED);
        application.setReviewedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        application.setReviewedBy(reviewer);
        applicationRepository.saveAndFlush(application);

        var issuedCard = creditCardService.issueCard(new CreditCardRequest(
                application.getCustomer().getCustomerId(),
                application.getCardType().getCardTypeId(),
                application.getRequestedCreditLimit()));
        application.setIssuedCard(
                creditCardRepository.findById(issuedCard.cardNumber())
                        .orElseThrow(() -> new IllegalStateException(
                                "Issued card was not persisted")));
        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public CreditCardApplicationResponse rejectApplication(
            String applicationId,
            String reviewerEmail,
            String reason) {
        CreditCardApplication application = findPendingApplication(applicationId);
        if (reason != null && reason.length() > 500) {
            throw new IllegalArgumentException(
                    "Rejection reason must not exceed 500 characters");
        }
        UserLogin reviewer = userLoginRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated reviewer account was not found"));
        application.setStatus(CreditCardApplicationStatus.REJECTED);
        application.setReviewedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        application.setReviewedBy(reviewer);
        application.setRejectionReason(
                reason == null || reason.isBlank() ? null : reason.trim());
        return toResponse(applicationRepository.save(application));
    }

    private CreditCardApplication findPendingApplication(String applicationId) {
        CreditCardApplication application = applicationRepository
                .findLockedByApplicationId(applicationId)
                .orElseThrow(() -> new CreditCardApplicationNotFoundException(
                        "Credit card application not found: " + applicationId));
        if (application.getStatus() != CreditCardApplicationStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending applications can be reviewed");
        }
        return application;
    }

    private CreditCardApplication findApplication(String applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CreditCardApplicationNotFoundException(
                        "Credit card application not found: " + applicationId));
    }

    private static CreditCardApplicationResponse toResponse(
            CreditCardApplication application) {
        String number = application.getIssuedCard() == null
                ? null : application.getIssuedCard().getCardNumber();
        return new CreditCardApplicationResponse(
                application.getApplicationId(),
                application.getCustomer().getCustomerId(),
                application.getCustomer().getName(),
                application.getCustomer().getEmail(),
                application.getCardType().getCardTypeId(),
                application.getCardType().getName(),
                application.getCardType().getNetwork(),
                application.getCardType().getDescription(),
                BigDecimal.valueOf(application.getCardType().getAnnualFee()),
                application.getRequestedCreditLimit(),
                application.getStatus(),
                application.getApplicationDate(),
                application.getReviewedDate(),
                application.getReviewedBy() == null
                        ? null : application.getReviewedBy().getEmail(),
                application.getRejectionReason(),
                number == null ? null : number.substring(number.length() - 4));
    }
}

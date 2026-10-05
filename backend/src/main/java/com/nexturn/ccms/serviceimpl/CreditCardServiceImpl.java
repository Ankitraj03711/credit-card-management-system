package com.nexturn.ccms.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import com.nexturn.ccms.service.CustomerAccessService;
import com.nexturn.ccms.dto.CreditCardRequest;
import com.nexturn.ccms.dto.CreditCardResponse;
import com.nexturn.ccms.entity.CardType;
import com.nexturn.ccms.entity.CreditCardDetails;
import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.enums.CardStatus;
import com.nexturn.ccms.exception.CardTypeNotFoundException;
import com.nexturn.ccms.exception.CreditCardNotFoundException;
import com.nexturn.ccms.exception.CustomerNotFoundException;
import com.nexturn.ccms.repository.CardTypeRepository;
import com.nexturn.ccms.repository.CreditCardDetailsRepository;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.service.CreditCardService;
import com.nexturn.ccms.service.IdGeneratorService;

@Service
@Transactional
public class CreditCardServiceImpl implements CreditCardService {

    private static final String CARD_NUMBER_SEQUENCE = "CARD_NUMBER";

    private static final int CARD_NUMBER_LENGTH = 16;

    private static final int CVV_BOUND = 1000;

    private static final int EXPIRY_YEARS = 5;

    private final CreditCardDetailsRepository creditCardRepository;
    private final CustomerRepository customerRepository;
    private final CardTypeRepository cardTypeRepository;
    private final IdGeneratorService idGeneratorService;
    private final CustomerAccessService customerAccessService;

    private final RandomGenerator randomGenerator =
            RandomGenerator.getDefault();

    public CreditCardServiceImpl(
            CreditCardDetailsRepository creditCardRepository,
            CustomerRepository customerRepository,
            CardTypeRepository cardTypeRepository,
            IdGeneratorService idGeneratorService,
            CustomerAccessService customerAccessService) {

        this.creditCardRepository = creditCardRepository;
        this.customerRepository = customerRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.idGeneratorService = idGeneratorService;
        this.customerAccessService = customerAccessService;
    }

    @Override
    public CreditCardResponse issueCard(CreditCardRequest request) {

        validateRequest(request);

        Customer customer =
                customerRepository.findById(request.customerId())
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer not found with id: "
                                                + request.customerId()));

        CardType cardType =
                cardTypeRepository.findById(request.cardTypeId())
                        .orElseThrow(() ->
                                new CardTypeNotFoundException(
                                        "Card type not found with id: "
                                                + request.cardTypeId()));

        String cardNumber = generateCardNumber();

        String cvv = generateCvv();

        LocalDate issueDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        LocalDate expiryDate =
                issueDate.plusYears(EXPIRY_YEARS);

        // Entity uses Double, so convert BigDecimal to Double
        Double creditLimit =
                request.creditLimit().doubleValue();

        CreditCardDetails card = new CreditCardDetails();

        card.setCardNumber(cardNumber);
        card.setCustomer(customer);
        card.setCardType(cardType);
        card.setCvv(cvv);
        card.setCreditLimit(creditLimit);
        card.setAvailableLimit(creditLimit);
        card.setOutstandingBalance(0.0);
        card.setIssueDate(issueDate);
        card.setExpiryDate(expiryDate);
        card.setCardStatus(CardStatus.ACTIVE);
        return toResponse(
                creditCardRepository.save(card));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardResponse> getAllCards() {

        return creditCardRepository.findAll()
                .stream()
                .map(card -> toResponse(card))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CreditCardResponse getCardByCardNumber(
            String cardNumber) {

        return toResponse(findCard(cardNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public String getCardCvv(
            String cardNumber,
            Authentication authentication) {

        CreditCardDetails card = findCard(cardNumber);
        customerAccessService.requireOwnCustomer(
                card.getCustomer().getCustomerId(), authentication);
        return card.getCvv();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardResponse> getCardsByCustomerId(
            Integer customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return creditCardRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(card -> toResponse(card))
                .toList();
    }

    @Override
    public CreditCardResponse activateCard(
            String cardNumber) {

        CreditCardDetails card = findCard(cardNumber);

        if (card.getCardStatus() == CardStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed card cannot be activated");
        }

        card.setCardStatus(CardStatus.ACTIVE);

        return toResponse(card);
    }

    @Override
    public CreditCardResponse blockCard(
            String cardNumber) {

        CreditCardDetails card = findCard(cardNumber);

        if (card.getCardStatus() == CardStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed card cannot be blocked");
        }

        card.setCardStatus(CardStatus.BLOCKED);

        return toResponse(card);
    }

    @Override
    public CreditCardResponse unblockCard(
            String cardNumber) {

        CreditCardDetails card = findCard(cardNumber);

        if (card.getCardStatus() == CardStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed card cannot be unblocked");
        }

        card.setCardStatus(CardStatus.ACTIVE);

        return toResponse(card);
    }

    @Override
    public CreditCardResponse closeCard(
            String cardNumber) {

        CreditCardDetails card = findCard(cardNumber);

        card.setCardStatus(CardStatus.CLOSED);

        return toResponse(card);
    }

    private CreditCardDetails findCard(
            String cardNumber) {

        return creditCardRepository.findById(cardNumber)
                .orElseThrow(() ->
                        new CreditCardNotFoundException(
                                "Credit card not found with number: "
                                        + cardNumber));
    }

    private String generateCardNumber() {

        long value = idGeneratorService.getNextValue(
                CARD_NUMBER_SEQUENCE);

        String cardNumber =
                String.format("%016d", value);

        if (cardNumber.length() != CARD_NUMBER_LENGTH) {
            throw new IllegalStateException(
                    "Generated card number is not 16 digits");
        }

        return cardNumber;
    }

    private String generateCvv() {

        return String.format(
                "%03d",
                randomGenerator.nextInt(CVV_BOUND)
        );
    }

    private static void validateRequest(
            CreditCardRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Credit card request cannot be null");
        }

        if (request.customerId() == null) {
            throw new IllegalArgumentException(
                    "Customer ID is required");
        }

        if (request.cardTypeId() == null) {
            throw new IllegalArgumentException(
                    "Card type ID is required");
        }

        if (request.creditLimit() == null ||
                request.creditLimit().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Credit limit must be greater than zero");
        }
    }

    private static CreditCardResponse toResponse(CreditCardDetails card) {

        return new CreditCardResponse(
                card.getCardNumber(),
                card.getCustomer().getCustomerId(),
                card.getCardType().getCardTypeId(),
                BigDecimal.valueOf(card.getCreditLimit()),
                BigDecimal.valueOf(card.getAvailableLimit()),
                BigDecimal.valueOf(card.getOutstandingBalance()),
                card.getIssueDate(),
                card.getExpiryDate(),
                card.getCardStatus(),
                card.getRemarks()
        );
    }
}
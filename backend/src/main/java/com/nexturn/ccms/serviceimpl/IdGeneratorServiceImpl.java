package com.nexturn.ccms.serviceimpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexturn.ccms.entity.CardType;
import com.nexturn.ccms.entity.CreditCardDetails;
import com.nexturn.ccms.entity.Customer;
import com.nexturn.ccms.entity.IdGenerator;
import com.nexturn.ccms.entity.Payment;
import com.nexturn.ccms.repository.CardTypeRepository;
import com.nexturn.ccms.repository.CreditCardDetailsRepository;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.repository.IdGeneratorRepository;
import com.nexturn.ccms.repository.PaymentRepository;
import com.nexturn.ccms.service.IdGeneratorService;

@Service
public class IdGeneratorServiceImpl implements IdGeneratorService {

    private final IdGeneratorRepository idGeneratorRepository;
    private final CustomerRepository customerRepository;
    private final CardTypeRepository cardTypeRepository;
    private final CreditCardDetailsRepository creditCardRepository;
    private final PaymentRepository paymentRepository;

    public IdGeneratorServiceImpl(
            IdGeneratorRepository idGeneratorRepository,
            CustomerRepository customerRepository,
            CardTypeRepository cardTypeRepository,
            CreditCardDetailsRepository creditCardRepository,
            PaymentRepository paymentRepository) {
        this.idGeneratorRepository = idGeneratorRepository;
        this.customerRepository = customerRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.creditCardRepository = creditCardRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public synchronized Long getNextValue(String sequenceName) {

        IdGenerator generator = idGeneratorRepository.findById(sequenceName)
                .orElseGet(() -> new IdGenerator(sequenceName, initialValue(sequenceName)));

        Long nextValue = generator.getCurrentValue() + 1;
        generator.setCurrentValue(nextValue);
        idGeneratorRepository.save(generator);
        return nextValue;
    }

    private long initialValue(String sequenceName) {
        return switch (sequenceName) {
            case "CUSTOMER_ID" -> customerRepository.findAll().stream()
                    .mapToLong(Customer::getCustomerId)
                    .max()
                    .orElse(0L);
            case "CARD_TYPE_ID" -> cardTypeRepository.findAll().stream()
                    .mapToLong(CardType::getCardTypeId)
                    .max()
                    .orElse(0L);
            case "CARD_NUMBER" -> creditCardRepository.findAll().stream()
                    .map(CreditCardDetails::getCardNumber)
                    .mapToLong(IdGeneratorServiceImpl::numericValue)
                    .max()
                    .orElse(0L);
            case "PAYMENT_REFERENCE" -> paymentRepository.findAll().stream()
                    .map(Payment::getPaymentReference)
                    .mapToLong(IdGeneratorServiceImpl::referenceValue)
                    .max()
                    .orElse(0L);
            default -> throw new IllegalArgumentException(
                    "Unknown ID sequence: " + sequenceName);
        };
    }

    private static long numericValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private static long referenceValue(String reference) {
        if (reference == null) {
            return 0L;
        }
        int separator = reference.lastIndexOf('-');
        if (separator < 0 || separator == reference.length() - 1) {
            return 0L;
        }
        return numericValue(reference.substring(separator + 1));
    }
}
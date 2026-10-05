package com.nexturn.ccms.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexturn.ccms.dto.CardTypeRequest;
import com.nexturn.ccms.dto.CardTypeResponse;
import com.nexturn.ccms.entity.CardType;
import com.nexturn.ccms.exception.CardTypeNotFoundException;
import com.nexturn.ccms.repository.CardTypeRepository;
import com.nexturn.ccms.service.CardTypeService;
import com.nexturn.ccms.service.IdGeneratorService;

@Service
@Transactional
public class CardTypeServiceImpl implements CardTypeService {

    private final CardTypeRepository cardTypeRepository;
    private final IdGeneratorService idGeneratorService;

    public CardTypeServiceImpl(
            CardTypeRepository cardTypeRepository,
            IdGeneratorService idGeneratorService) {
        this.cardTypeRepository = cardTypeRepository;
        this.idGeneratorService = idGeneratorService;
    }

    @Override
    public CardTypeResponse createCardType(
            CardTypeRequest request) {

        validate(request);

        var cardType = new CardType(
                request.name(),
                request.description(),
                request.annualFee().doubleValue()
        );
        cardType.setNetwork(request.network());
        cardType.setCardTypeId(
                Math.toIntExact(idGeneratorService.getNextValue("CARD_TYPE_ID"))
        );

        return toResponse(cardTypeRepository.save(cardType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardTypeResponse> getAllCardTypes() {
        return cardTypeRepository.findAll()
                .stream()
                .map(CardTypeServiceImpl::toResponse)
                .toList();
    } 

    @Override
    @Transactional(readOnly = true)
    public CardTypeResponse getCardTypeById(
            Integer cardTypeId) {

        return toResponse(findCardType(cardTypeId));
    }

    @Override
    public CardTypeResponse updateCardType(
            Integer cardTypeId,
            CardTypeRequest request) {

        validate(request);

        var cardType = findCardType(cardTypeId);

        cardType.setName(request.name());
        cardType.setDescription(request.description());
        cardType.setAnnualFee(request.annualFee().doubleValue());
        cardType.setNetwork(request.network());

        return toResponse(cardType);
    }

    @Override
    public void deleteCardType(Integer cardTypeId) {

        cardTypeRepository.delete(findCardType(cardTypeId));
    }

    private CardType findCardType(Integer id) {

        return cardTypeRepository.findById(id)
                .orElseThrow(() ->
                        new CardTypeNotFoundException(
                                "Card type not found with id: " + id));
    }

    private static void validate(CardTypeRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Card type request cannot be null");
        }

        if (request.name() == null ||
                request.name().isBlank()) {

            throw new IllegalArgumentException(
                    "Card type name is required");
        }

        if (request.annualFee() == null ||
                request.annualFee().signum() < 0) {

            throw new IllegalArgumentException(
                    "Annual fee cannot be negative");
        }
    }

    private static CardTypeResponse toResponse(
            CardType cardType) {

    	return new CardTypeResponse(
    	        cardType.getCardTypeId(),
    	        cardType.getName(),
    	        cardType.getDescription(),
    	        BigDecimal.valueOf(cardType.getAnnualFee()),
    	        cardType.getNetwork()
    	);
    }
}
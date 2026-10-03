package com.nexturn.ccms.service;

import java.util.List;

import com.nexturn.ccms.dto.CardTypeRequest;
import com.nexturn.ccms.dto.CardTypeResponse;

public interface CardTypeService {

    CardTypeResponse createCardType(CardTypeRequest request);

    List<CardTypeResponse> getAllCardTypes();

    CardTypeResponse getCardTypeById(Integer cardTypeId);

    CardTypeResponse updateCardType(
            Integer cardTypeId,
            CardTypeRequest request);

    void deleteCardType(Integer cardTypeId);
}
package com.nexturn.ccms.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nexturn.ccms.entity.IdGenerator;
import com.nexturn.ccms.repository.IdGeneratorRepository;
import com.nexturn.ccms.service.IdGeneratorService;

@Service
public class IdGeneratorServiceImpl implements IdGeneratorService {

    @Autowired
    IdGeneratorRepository idGeneratorRepository;

    @Override
    public Long getNextValue(String sequenceName) {

        IdGenerator generator = idGeneratorRepository.findById(sequenceName)
                .orElseThrow(() -> new RuntimeException("Sequence not found: " + sequenceName));

        Long nextValue = generator.getCurrentValue() + 1;
        generator.setCurrentValue(nextValue);
        idGeneratorRepository.save(generator);
        return nextValue;
    }
}
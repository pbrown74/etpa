package com.etpa.electric.service;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.entity.Consumption;
import com.etpa.electric.exception.ConsumptionNotFoundException;
import com.etpa.electric.repository.ConsumptionRepository;
import com.etpa.electric.utils.Month;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * a service layer for interacting with consumptions
 */
@Service
public class ConsumptionService {
    private static Logger logger = LoggerFactory.getLogger(ConsumptionService.class);

    @Autowired
    private ConsumptionRepository consumptionRepository;

    public ConsumptionDTO get(final Month month, final String metreId) {
        Optional<Consumption> cons = consumptionRepository.findByMonthCodeAndMetreId(month.getCode(), metreId);
        if(cons.isPresent()){
            return buildConsumption(cons.get());
        }
        else{
            throw new ConsumptionNotFoundException(metreId, month);
        }
    }

    private ConsumptionDTO buildConsumption(final Consumption c){
        ConsumptionDTO dto = new ConsumptionDTO(c.getId(),
                c.getMetreId(),
                c.getMonth().name(),
                c.getProfile(),
                c.getConsumption());
        return dto;
    }

}
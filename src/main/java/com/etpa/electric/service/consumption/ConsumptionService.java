package com.etpa.electric.service.consumption;

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
 * a service layer for managing consumptions
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

    public void delete(final String consumptionId){
        consumptionRepository.deleteById(consumptionId);
        logger.debug("Deleted consumption: "+ consumptionId);
    }

    public ConsumptionDTO save(final ConsumptionDTO consumption){
        return buildConsumption(consumptionRepository.save(buildConsumption(consumption)));
    }

    public ConsumptionDTO save(final ConsumptionDTO consumption, final String id){
        if(!consumptionRepository.findById(id).isPresent()){
            throw new ConsumptionNotFoundException(id);
        }
        return buildConsumption(consumptionRepository.save(buildConsumption(consumption, id)));
    }

    private ConsumptionDTO buildConsumption(final Consumption c){
        ConsumptionDTO dto = new ConsumptionDTO(
                c.getId(),
                c.getMetreId(),
                c.getMonth().name(),
                c.getProfile(),
                c.getConsumption());
        return dto;
    }

    private Consumption buildConsumption(final ConsumptionDTO dto){
        return buildConsumption(dto, null);
    }

    private Consumption buildConsumption(final ConsumptionDTO dto, final String id){
        Consumption c = new Consumption();
        if(id!=null){
            c.setId(id);
        }
        c.setMetreId(dto.getMetreId());
        c.setMonth(dto.getMonth());
        c.setProfile(dto.getProfile());
        c.setConsumption(dto.getConsumption());
        return c;
    }

}
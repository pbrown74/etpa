package com.etpa.electric.service.metre;

import com.etpa.electric.dto.*;
import com.etpa.electric.entity.Consumption;
import com.etpa.electric.entity.MetreReading;
import com.etpa.electric.repository.ConsumptionRepository;
import com.etpa.electric.repository.MetreReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * a service layer for interacting with MetreReadings, validations are located here.
 * the design is to save as many as we can and report errors for the ones which failed.
 */
@Service
public class MetreReadingSaver {
    private static Logger logger = LoggerFactory.getLogger(MetreReadingSaver.class);

    private List<MetreReading> toSaveReadings = new ArrayList<>();
    private List<Consumption> toSaveCons = new ArrayList<>();

    @Autowired
    private MetreReadingBuilder metreReadingBuilder;

    @Autowired
    private MetreReadingRepository metreReadingRepository;

    @Autowired
    private ConsumptionRepository consumptionRepository;

    public void save(final MetreReadingDTO reading, final ConsumptionDTO consumption){
        toSaveReadings.add(metreReadingBuilder.buildMetreReading(reading));
        toSaveCons.add(buildConsumption(consumption));
    }

    public List<MetreReading> commit(){
        List<MetreReading> savedReadings = metreReadingRepository.saveAll(toSaveReadings);
        List<Consumption> savedConsumptions = consumptionRepository.saveAll(toSaveCons);
        if(logger.isDebugEnabled()){
            logger.debug("Saved "+savedReadings.size()+" metre readings") ;
            logger.debug("Saved "+savedConsumptions.size()+" consumptions") ;
        }
        return savedReadings;
    }

    public void clear(){
        toSaveReadings.clear();
        toSaveCons.clear();
    }

    private Consumption buildConsumption(final ConsumptionDTO dto){
        Consumption c = new Consumption();
        c.setConsumption(dto.getConsumption());
        c.setMetreId(dto.getMetreId());
        c.setMonth(dto.getMonth());
        c.setProfile(dto.getProfile());
        return c;
    }

}
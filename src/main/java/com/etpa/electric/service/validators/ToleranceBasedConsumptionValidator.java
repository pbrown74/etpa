package com.etpa.electric.service.validators;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.entity.Fraction;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.repository.FractionRepository;
import com.etpa.electric.utils.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ToleranceBasedConsumptionValidator implements IConsumptionValidator{

    @Autowired
    private  FractionRepository fractionRepository;

    public void validateConsumption(ConsumptionDTO consumption, List<MetreReadingDTO> readingsPerMetre) throws ValidationException {
        // first check if there is a valid fraction for the month
        Optional<Fraction> fraction = this.fractionRepository.findByProfileAndMonthCode(consumption.getProfile(), consumption.getMonth().getCode());
        if(!fraction.isPresent()){
            throw new ValidationException(Errors.INVALID_CONSUMPTION_FOR_MONTH,
                    "Consumption could not be calculated for metre: " + consumption.getMetreId() +
                            " and month: " + consumption.getMonth().name()+
                            " because of missing fraction definition for (profile="+consumption.getProfile()+
                            ",month="+consumption.getMonth().name()+")");
        }        // the total consumption is the highest value, we know the last value is the highest due to previous validation
        Optional<BigDecimal> totalConsumption = readingsPerMetre.stream().map(rm->rm.getMetreReading()).max(BigDecimal::compareTo);
        if(totalConsumption.isPresent()){
            // check if the consumption is within tolerance
            Fraction fractionForMonth = fraction.get();
            BigDecimal minCons = totalConsumption.get().multiply(fractionForMonth.getFraction()).multiply(BigDecimal.valueOf(0.75));
            BigDecimal maxCons = totalConsumption.get().multiply(fractionForMonth.getFraction()).multiply(BigDecimal.valueOf(1.25));
            BigDecimal cons = consumption.getConsumption();
            boolean isWithinRange = cons.compareTo(minCons)>=0 && cons.compareTo(maxCons)<=0;
            if(!isWithinRange){
                throw new ValidationException(Errors.INVALID_CONSUMPTION_FOR_MONTH,
                        "Consumption: " + cons +
                                " invalid for metre: " + consumption.getMetreId() + " and month: " + consumption.getMonth().name()+
                                " not within 25% tolerance of fraction: " + fractionForMonth.getFraction() +
                                " of total consumption: "+ totalConsumption.get());
            }
        }
    }

}

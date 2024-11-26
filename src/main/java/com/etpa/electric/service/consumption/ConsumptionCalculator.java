package com.etpa.electric.service.consumption;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.service.validators.IConsumptionValidator;
import com.etpa.electric.utils.Errors;
import com.etpa.electric.utils.Month;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component("basicConsumptionCalculator")
public class ConsumptionCalculator implements IConsumptionCalculator {

    @Autowired
    private List<IConsumptionValidator> consumptionValidators;

    /**
     * Here we calc the consumption from the current reading and the prev one.
     * we ensure a valid consumption before returning the ConsumptionDTO to caller.
     *
     * @param currReading
     * @param readingsPerMetre
     * @return
     */
    public ConsumptionDTO calculate(final MetreReadingDTO currReading, final List<MetreReadingDTO> readingsPerMetre){
        MetreReadingDTO prevReading = findPreviousReading(currReading, readingsPerMetre);
        BigDecimal consumption = currReading.getMetreReading().subtract(prevReading.getMetreReading());
        ConsumptionDTO newConsumption = new ConsumptionDTO(currReading.getMetreId(),
                currReading.getMonth().name(),
                currReading.getProfile(),
                consumption);
        validateConsumption(newConsumption, readingsPerMetre);
        return newConsumption;
    }

    /**
     * if the current reading you are looking at is JAN, then the previous one is taken as 0 in
     * order to calculate the right consumption given that we reset to 0 at start of the year.
     * @param reading
     * @param readingsPerMetre
     * @return
     */
    private MetreReadingDTO findPreviousReading(final MetreReadingDTO reading, final List<MetreReadingDTO> readingsPerMetre){
        if(reading.getMonth()== Month.JAN){
            MetreReadingDTO zero = new MetreReadingDTO("-1", reading.getMetreId(),reading.getProfile(),Month.DEC,BigDecimal.ZERO);
            return zero;
        }
        else{
            Month prevMonth = Month.prev(reading.getMonth());
            Optional<MetreReadingDTO> prevReading = readingsPerMetre.stream().filter(r->r.getMonth()==prevMonth).findFirst();
            if(prevReading.isPresent()){
                return prevReading.get();
            }
            else{
                throw new ValidationException(Errors.MISSING_PREV_READING,
                        "No previous reading found for metre: " +
                                reading.getMetreId() + " month: " + reading.getMonth().name());
            }
        }
    }

    private void validateConsumption(ConsumptionDTO consumption, List<MetreReadingDTO> readingsPerMetre){
        consumptionValidators.forEach(v->v.validateConsumption(consumption, readingsPerMetre));
    }

}

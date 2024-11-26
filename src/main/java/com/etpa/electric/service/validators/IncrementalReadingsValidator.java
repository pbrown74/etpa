package com.etpa.electric.service.validators;

import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.utils.Errors;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

@Service
public class IncrementalReadingsValidator implements IReadingsValidator{

    public void validateMetreReadings(List<MetreReadingDTO> readingsPerMetre) throws ValidationException{
        List<MetreReadingDTO> copy = new Vector(readingsPerMetre);
        // this will sort them by Month
        Collections.sort(copy);
        // now check each metre reading is lower than next one
        MetreReadingDTO prev = copy.get(0);
        for(int i=1;i<copy.size();i++){
            MetreReadingDTO reading = copy.get(i);
            if(reading.getMetreReading().doubleValue() < prev.getMetreReading().doubleValue()){
                throw new ValidationException(Errors.FILE_MONTHS_DECREMENT_FOR_METRE,
                        "Metre readings must increment month to month: "+
                                reading.getMetreReading() + " is less than " + prev.getMetreReading());
            }
            prev = reading;
        }
    }

}

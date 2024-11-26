package com.etpa.electric.service.validators;

import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.entity.Fraction;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.repository.FractionRepository;
import com.etpa.electric.utils.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

@Service
public class ExistingFractionsValidator implements IReadingsValidator{

    @Autowired
    private FractionRepository fractionRepository;

    public void validateMetreReadings(List<MetreReadingDTO> readingsPerMetre) throws ValidationException{
        readingsPerMetre.stream().map(reading -> reading.getProfile()).forEach(profile->{
            List<Fraction> fractions = this.fractionRepository.findByProfile(profile);
            if(fractions.isEmpty()){
                throw new ValidationException(Errors.PROFILE_NONEXISTANT,
                        "Profile not found for metre reading: " + profile);
            }
        });
    }

}

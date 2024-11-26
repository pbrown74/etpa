package com.etpa.electric.service.validators;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.exception.ValidationException;

import java.util.List;

public interface IConsumptionValidator {

    void validateConsumption(ConsumptionDTO cons, List<MetreReadingDTO> readingsPerMetre) throws ValidationException;

}

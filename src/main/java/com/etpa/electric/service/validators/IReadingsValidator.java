package com.etpa.electric.service.validators;

import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.exception.ValidationException;

import java.util.List;

public interface IReadingsValidator {

    void validateMetreReadings(List<MetreReadingDTO> readingsPerMetre) throws ValidationException;

}

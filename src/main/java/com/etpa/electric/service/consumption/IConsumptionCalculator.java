package com.etpa.electric.service.consumption;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.utils.Errors;
import com.etpa.electric.utils.Month;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IConsumptionCalculator {

    ConsumptionDTO calculate(final MetreReadingDTO currReading, final List<MetreReadingDTO> readingsPerMetre) throws ValidationException;

}

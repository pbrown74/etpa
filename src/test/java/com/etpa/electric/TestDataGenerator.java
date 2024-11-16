package com.etpa.electric;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.utils.Errors;
import com.etpa.electric.utils.Month;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * generate fractions from the readings, which will pass the validations.
 * you can paste the output into the CSV files used for testing.
 */
public class TestDataGenerator {

    public static ConsumptionDTO calculateConsumption(MetreReadingDTO currReading, List<MetreReadingDTO> readingsPerMetre){
        MetreReadingDTO prevReading = findPreviousReading(currReading, readingsPerMetre);
        BigDecimal consumption = currReading.getMetreReading().subtract(prevReading.getMetreReading());
        return new ConsumptionDTO(currReading.getMetreId(), currReading.getMonth().name(), currReading.getProfile(), consumption);
    }

    public static  MetreReadingDTO findPreviousReading(MetreReadingDTO reading, List<MetreReadingDTO> readingsPerMetre){
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

    public static void main(String[] args) throws Exception {
        List<MetreReadingDTO> readings = new Vector<>();
        // MeterID,Profile,Month,Meter reading
        Files.lines(Path.of("src/main/resources/test1_readings.csv"))
                .skip(1)
                .forEach(l->{
                    StringTokenizer st = new StringTokenizer(l,",");
                    readings.add(new MetreReadingDTO(
                            "-1",
                            st.nextToken(),
                            st.nextToken(),
                            Month.valueOf(st.nextToken()),
                            BigDecimal.valueOf(Double.valueOf(st.nextToken()))));
                    });
        System.out.println("Month,Profile,Fraction");
        Map<String, List<MetreReadingDTO>> groupedReadingsPerMetre = readings.stream()
                .collect(Collectors.groupingBy(reading -> reading.getMetreId()));
        List<String> ids = Arrays.asList("0001","0004");
        for(String id : ids){
            List<MetreReadingDTO> readingsPerMetre = groupedReadingsPerMetre.get(id);
            BigDecimal totalConsumption = readingsPerMetre.stream().map(rm->rm.getMetreReading()).max(BigDecimal::compareTo).get();
            for (MetreReadingDTO reading : readings) {
                if(reading.getMetreId().equals(id)){
                    ConsumptionDTO consumption = calculateConsumption(reading, readingsPerMetre);
                    System.out.print(reading.getMonth().name());
                    System.out.print(",");
                    System.out.print(reading.getProfile());
                    System.out.print(",");
                    BigDecimal frac = consumption.getConsumption().divide(totalConsumption, 4, RoundingMode.HALF_UP);
                    System.out.print(frac);
                    System.out.println();
                }
            }
        }
    }

}

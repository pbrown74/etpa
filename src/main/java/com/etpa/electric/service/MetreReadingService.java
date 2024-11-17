package com.etpa.electric.service;

import com.etpa.electric.dto.*;
import com.etpa.electric.entity.Consumption;
import com.etpa.electric.entity.Fraction;
import com.etpa.electric.entity.MetreReading;
import com.etpa.electric.exception.BadFileUploadException;
import com.etpa.electric.exception.MetreReadingNotFoundException;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.repository.ConsumptionRepository;
import com.etpa.electric.repository.FractionRepository;
import com.etpa.electric.repository.MetreReadingRepository;
import com.etpa.electric.utils.Errors;
import com.etpa.electric.utils.Month;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * a service layer for interacting with MetreReadings, validations are located here.
 * the design is to save as many as we can and report errors for the ones which failed.
 */
@Service
public class MetreReadingService {
    private static Logger logger = LoggerFactory.getLogger(MetreReadingService.class);

    @Autowired
    private MetreReadingRepository metreReadingRepository;

    @Autowired
    private FractionRepository fractionRepository;

    @Autowired
    private ConsumptionRepository consumptionRepository;

    @Transactional
    public ResponseDTO save(final MultipartFile metreReadingsCsv) {
        List<ItemDTO> items = new Vector<>();
        List<ErrorDTO> errors = new Vector<>();
        try {
            List<MetreReadingDTO> readingDtos = new Vector<>();
            InputStream inputStream = metreReadingsCsv.getInputStream();
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .skip(1)
                    .forEach(l->{
                        readingDtos.add(newLine(l));
                    });
            // readings are unordered so we have to have them all in memory to do validation
            Map<String, List<MetreReadingDTO>> groupedReadingsPerMetre = readingDtos.stream()
                    .collect(Collectors.groupingBy(reading -> reading.getMetreId()));
            // per metre, we look at the set of readings
            groupedReadingsPerMetre.forEach((metreId, readingsPerMetre) -> {
                try{
                    // for one metre we look at its readings
                    validateReadingsIncrement(readingsPerMetre);
                    validateFractionsExistForMonth(readingsPerMetre);
                    List<MetreReading> toSaveReadings = new Vector<>();
                    List<Consumption> toSaveCons = new Vector<>();
                    // generate a consumption for each reading, if one fails then skip the metre
                    readingsPerMetre.stream().forEach(reading->{
                        // if data for any metre reading fails validation then we skip the entire metre
                        ConsumptionDTO consumption = calculateConsumption(reading, readingsPerMetre);
                        validateConsumptionForMonth(consumption, readingsPerMetre);
                        toSaveReadings.add(buildMetreReading(reading));
                        toSaveCons.add(buildConsumption(consumption));
                    });
                    // if we made it here, no metre reading failed validation, so we save all the readings
                    // and consumptions. these saves are transactional across repos.
                    List<MetreReading> savedReadings = metreReadingRepository.saveAll(toSaveReadings);
                    List<Consumption> savedConsumptions = consumptionRepository.saveAll(toSaveCons);
                    savedReadings.forEach(r->items.add(new ItemDTO(buildMetreReading(r))));
                    if(logger.isDebugEnabled()){
                        logger.debug("Saved "+savedReadings.size()+" metre readings") ;
                        logger.debug("Saved "+savedConsumptions.size()+" consumptions") ;
                    }
                }
                catch(ValidationException e){
                    // after here we go to the next metre if there is one
                    String err = "Skipped metre reading due to validation error";
                    logger.error(err, e);
                    ErrorDTO error = newError(e.getMessage(), e.getCode());
                    errors.add(error);
                }
            });
        }
        catch(Exception e) {
            logger.error("Could not read metre reading file: " + metreReadingsCsv.getName(), e);
            throw new BadFileUploadException(metreReadingsCsv.getName());
        }
        return new ResponseDTO(items, errors);
    }

    public MetreReadingDTO save(final MetreReadingDTO reading, final String id){
        if(!metreReadingRepository.findById(id).isPresent()){
            throw new MetreReadingNotFoundException(id);
        }
        return buildMetreReading(metreReadingRepository.save(buildMetreReading(reading, id)));
    }

    public void delete(final String metreReadingId) {
        this.metreReadingRepository.deleteById(metreReadingId);
        logger.debug("Deleted metre reading: "+ metreReadingId);
    }

    public MetreReadingDTO get(final String metreReadingId) {
        Optional<MetreReading> reading = this.metreReadingRepository.findById(metreReadingId);
        if(reading.isPresent()){
            return buildMetreReading(reading.get());
        }
        else{
            throw new MetreReadingNotFoundException(metreReadingId);
        }
    }

    private ConsumptionDTO calculateConsumption(final MetreReadingDTO currReading, final List<MetreReadingDTO> readingsPerMetre){
        MetreReadingDTO prevReading = findPreviousReading(currReading, readingsPerMetre);
        BigDecimal consumption = currReading.getMetreReading().subtract(prevReading.getMetreReading());
        return new ConsumptionDTO(currReading.getMetreId(), currReading.getMonth().name(), currReading.getProfile(), consumption);
    }

    /**
     * if the current reading you are looking at it JAN, then the previous one is taken as 0 in
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

    /**
     * parse the line to a DTO
     * @param s
     * @return
     */
    private MetreReadingDTO newLine(final String s)  {
        StringTokenizer st = new StringTokenizer(s, ",");
        String metreId = st.nextToken();
        String profile = st.nextToken();
        String month = st.nextToken();
        BigDecimal metreReading = BigDecimal.valueOf(Double.valueOf(st.nextToken()));
        MetreReadingDTO dto = new MetreReadingDTO("-1", metreId, profile, Month.valueOf(month), metreReading);
        return dto;
    }

    private ErrorDTO newError(final String s, final int code){
        return new ErrorDTO(s, code);
    }

    /**
     * validation methods follow
     */

    private void validateReadingsIncrement(final List<MetreReadingDTO> readingsPerMetre){
        List<BigDecimal> readings = readingsPerMetre.stream().map(rm->rm.getMetreReading()).collect(Collectors.toList());
        List<BigDecimal> copy = new Vector<>(readings);
        // if the sort asc list is the same as the unsorted list, the values were ascending.
        // since its a small list this is acceptable. i might go for something more efficient
        // if we are dealing with large upload files and this code is called a lot.
        Collections.sort(readings);
        if(!copy.equals(readings)){
            throw new ValidationException(Errors.FILE_MONTHS_DECREMENT_FOR_METRE,
                    "Metre readings must increment month to month");
        }
    }

    private void validateFractionsExistForMonth(final List<MetreReadingDTO> readings){
        readings.stream().map(reading -> reading.getProfile()).forEach(profile->{
            List<Fraction> fractions = this.fractionRepository.findByProfile(profile);
            if(fractions.isEmpty()){
                throw new ValidationException(Errors.PROFILE_NONEXISTANT,
                        "Profile not found for metre reading: " + profile);
            }
        });
    }

    private void validateConsumptionForMonth(final ConsumptionDTO consumption, final List<MetreReadingDTO> readingsPerMetre){
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

    private MetreReading buildMetreReading(final MetreReadingDTO dto, final String id){
        MetreReading mr = new MetreReading();
        if(id!=null){
            mr.setId(id);
        }
        mr.setMetreReading(dto.getMetreReading());
        mr.setMetreId(dto.getMetreId());
        mr.setMonth(dto.getMonth());
        mr.setProfile(dto.getProfile());
        return mr;
    }

    private MetreReading buildMetreReading(final MetreReadingDTO dto){
        MetreReading mr = new MetreReading();
        mr.setMetreReading(dto.getMetreReading());
        mr.setMetreId(dto.getMetreId());
        mr.setMonth(dto.getMonth());
        mr.setProfile(dto.getProfile());
        return mr;
    }

    private MetreReadingDTO buildMetreReading(final MetreReading mr){
        return new MetreReadingDTO(mr.getId(), mr.getMetreId(), mr.getProfile(), mr.getMonth(), mr.getMetreReading());
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
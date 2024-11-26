package com.etpa.electric.service.metre;

import com.etpa.electric.dto.*;
import com.etpa.electric.entity.MetreReading;
import com.etpa.electric.exception.BadFileUploadException;
import com.etpa.electric.exception.MetreReadingNotFoundException;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.repository.FractionRepository;
import com.etpa.electric.repository.MetreReadingRepository;
import com.etpa.electric.service.consumption.IConsumptionCalculator;
import com.etpa.electric.service.validators.IReadingsValidator;
import com.etpa.electric.utils.MetreCSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a service layer for managing MetreReadings. the design is to save as many as we
 * can and report errors for the ones which failed.
 */
@Service
public class MetreReadingService implements IMetreReadingService {
    private static Logger logger = LoggerFactory.getLogger(MetreReadingService.class);

    @Autowired
    private FractionRepository fractionRepository;

    @Autowired
    private MetreReadingRepository metreReadingRepository;

    @Autowired
    private MetreCSVReader csvReader;

    @Autowired
    private List<IReadingsValidator> readingsValidators;

    @Autowired
    @Qualifier("basicConsumptionCalculator")
    private IConsumptionCalculator consumptionCalculator;

    @Autowired
    private MetreReadingBuilder metreReadingBuilder;

    @Autowired
    private MetreReadingSaver saver;

    @Transactional
    public ResponseDTO save(final MultipartFile metreReadingsCsv) {
        List<ItemDTO> items = new ArrayList<>();
        List<ErrorDTO> errors = new ArrayList<>();
        try {
            List<MetreReadingDTO> readingDtos = csvReader.read(metreReadingsCsv);
            // readings are unordered so we have to have them all in memory to do validation
            Map<String, List<MetreReadingDTO>> groupedReadingsPerMetreId = readingDtos.stream()
                    .collect(Collectors.groupingBy(reading -> reading.getMetreId()));
            // per metre, we look at the set of readings
            groupedReadingsPerMetreId.forEach((metreId, readingsPerMetre) -> {
                try{
                    saver.clear();
                    readingsValidators.forEach(v->v.validateMetreReadings(readingsPerMetre));
                    readingsPerMetre.stream().forEach(reading->{
                        // if data for any metre reading fails validation then we skip that metre
                        ConsumptionDTO consumption = consumptionCalculator.calculate(reading, readingsPerMetre);
                        saver.save(reading, consumption);
                    });
                    // if we made it here, no metre reading failed validation, so we save all the readings
                    // and consumptions. these saves are transactional across repos.
                    saver.commit().forEach(r->items.add(new ItemDTO(metreReadingBuilder.buildMetreReading(r))));
                }
                catch(ValidationException e){
                    // do not propagate exception (see README), we build up a json structure
                    // to return per-line result with HTTP 200 (200 even if it contains some errors)
                    String err = "Skipped metre reading due to validation error";
                    logger.error(err, e);
                    errors.add(new ErrorDTO(e.getMessage(), e.getCode()));
                }
            });
        }
        catch(Exception e) {
            logger.error("Could not read metre reading file: " + metreReadingsCsv.getName(), e);
            throw new BadFileUploadException(metreReadingsCsv.getName());
        }
        return new ResponseDTO(items, errors);
    }

    public MetreReadingDTO update(final MetreReadingDTO reading, final String id){
        if(!metreReadingRepository.findById(id).isPresent()){
            throw new MetreReadingNotFoundException(id);
        }
        return metreReadingBuilder.buildMetreReading(metreReadingRepository.save(metreReadingBuilder.buildMetreReading(reading, id)));
    }

    public void delete(final String metreReadingId) {
        this.metreReadingRepository.deleteById(metreReadingId);
        logger.debug("Deleted metre reading: "+ metreReadingId);
    }

    public MetreReadingDTO get(final String metreReadingId) {
        Optional<MetreReading> reading = this.metreReadingRepository.findById(metreReadingId);
        if(reading.isPresent()){
            return metreReadingBuilder.buildMetreReading(reading.get());
        }
        else{
            throw new MetreReadingNotFoundException(metreReadingId);
        }
    }

}
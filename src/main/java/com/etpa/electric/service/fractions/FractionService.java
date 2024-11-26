package com.etpa.electric.service.fractions;

import com.etpa.electric.dto.*;
import com.etpa.electric.entity.Fraction;
import com.etpa.electric.exception.BadFileUploadException;
import com.etpa.electric.exception.FractionNotFoundException;
import com.etpa.electric.exception.ValidationException;
import com.etpa.electric.repository.FractionRepository;
import com.etpa.electric.utils.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * a service layer for managing Fractions. the design is to save as many as we can
 * and report errors for the ones which failed.
 */
@Service
public class FractionService {
    private static Logger logger = LoggerFactory.getLogger(FractionService.class);

    @Autowired
    private FractionRepository fractionRepository;

    public ResponseDTO save(final MultipartFile fractionsCsv) {
        List<ItemDTO> items = new Vector<>();
        List<ErrorDTO> errors = new Vector<>();
        try {
            // skip header and convert lines to pojos
            List<FractionDTO> fractionDtos = new Vector<>();
            InputStream inputStream = fractionsCsv.getInputStream();
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .skip(1)
                    .forEach(l->{
                        fractionDtos.add(newLine(l));
                    });
            // readings are unordered so we have to have them all in memory to do validation
            Map<String, List<FractionDTO>> groupedFractionsPerProfile = fractionDtos.stream()
                    .collect(Collectors.groupingBy(fractions -> fractions.getProfile()));
            groupedFractionsPerProfile.forEach((profile, fractionsPerProfile) -> {
                try{
                    // could raise a validation error and then we try the next group of profile fractions.
                    // if validation fails then we wont add the fraction into the response items list and
                    // we wont save it to the repo.
                    validateFractionsSumToOne(fractionsPerProfile, profile);
                    // convert the DTOs to Hibernate entities and pass to the repo layer in a batch
                    List<Fraction> toSave = fractionsPerProfile.stream().map(fractionDto->buildFraction(fractionDto)).collect(Collectors.toList());
                    List<Fraction> saved = fractionRepository.saveAll(toSave);
                    // to simplify, this happens last after validation and save worked for the group of fractions
                    saved.stream().forEach(f->items.add(new ItemDTO(buildFraction(f))));
                    if(logger.isDebugEnabled()){
                        logger.debug("Saved " + saved.size() + " fractions for profile " + profile);
                        toSave.stream().forEach(f->logger.debug("Month: " + f.getMonth() +
                                ", fraction: " + f.getFraction().toString()));
                    }
                }
                catch(ValidationException e){
                    String err = "Skipped fractions for profile "+ profile +" due to validation error";
                    logger.error(err, e);
                    ErrorDTO error = newError(e.getMessage(), e.getCode());
                    errors.add(error);
                }
            });
        }
        catch(Exception e) {
            logger.error("Could not read fraction file: " + fractionsCsv.getName(), e);
            throw new BadFileUploadException(fractionsCsv.getName());
        }
        return new ResponseDTO(items, errors);
    }

    public FractionDTO save(final FractionDTO fraction, final String id){
        if(!fractionRepository.findById(id).isPresent()){
            throw new FractionNotFoundException(id);
        }
        return buildFraction(fractionRepository.save(buildFraction(fraction, id)));
    }

    public FractionDTO save(final FractionDTO fraction){
        return buildFraction(fractionRepository.save(buildFraction(fraction)));
    }

    public void delete(final String fractionId) {
        this.fractionRepository.deleteById(fractionId);
        logger.debug("Deleted fraction: "+ fractionId);
    }

    public FractionDTO get(final String fractionId) {
        Optional<Fraction> fraction = this.fractionRepository.findById(fractionId);
        if(fraction.isPresent()){
            return buildFraction(fraction.get());
        }
        else{
            throw new FractionNotFoundException(fractionId);
        }
    }

    private FractionDTO newLine(final String s)  {
        StringTokenizer st = new StringTokenizer(s, ",");
        String month = st.nextToken();
        String profile = st.nextToken();
        String fraction = st.nextToken();
        FractionDTO dto = new FractionDTO(month, profile, BigDecimal.valueOf(Double.valueOf(fraction)));
        return dto;
    }

    private ErrorDTO newError(final String s, final int code){
        return new ErrorDTO(s, code);
    }

    /**
     * validation methods follow
     */

    private void validateFractionsSumToOne(final List<FractionDTO> fractionsPerProfile, final String profile){
        BigDecimal sum = fractionsPerProfile.stream().map(fraction->fraction.getFraction())
                .reduce(BigDecimal.ZERO, BigDecimal::add).round(new MathContext(4));
        logger.debug("Fractions for profile " + profile + " summed to " + sum);
        if(BigDecimal.ONE.compareTo(sum)!=0){
            // this index is safe because there would not be a frac group for the profile without at least one frac
            throw new ValidationException(Errors.FRACTIONS_DONT_SUM_TO_ONE,
                    "Fractions for profile "+profile+" dont sum to 1.0, sum: " + sum);
        }
    }

    private Fraction buildFraction(final FractionDTO dto){
        Fraction f = new Fraction();
        f.setFraction(dto.getFraction());
        f.setMonth(dto.getMonth());
        f.setProfile(dto.getProfile());
        return f;
    }

    private Fraction buildFraction(final FractionDTO dto, final String id){
        Fraction f = new Fraction();
        if(id!=null){
            f.setId(id);
        }
        f.setFraction(dto.getFraction());
        f.setMonth(dto.getMonth());
        f.setProfile(dto.getProfile());
        return f;
    }

    private FractionDTO buildFraction(final Fraction dto){
        FractionDTO f = new FractionDTO(dto.getId(),dto.getMonth().name(),dto.getProfile(),dto.getFraction());
        return f;
    }

}
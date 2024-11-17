package com.etpa.electric.controller;

import com.etpa.electric.dto.FractionDTO;
import com.etpa.electric.dto.ResponseDTO;
import com.etpa.electric.service.FractionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * only the file upload endpoint was implemented because it is unclear whether the other
 * endpoints should be file based or done on the entity level. if entity level then there
 * are plenty more validations to think about regarding leaving a group of fractions in an
 * invalid state. And also, should a fraction be allowed to be updated if there is a metre
 * reading that was generated off it? perhaps MetreReading needs a foreign key into Fraction,
 * plenty more to think about if the extra endpoints are attempted.
 */
@RestController
@RequestMapping(path="/etpa")
public class FractionController {
    @Autowired
    private FractionService fractionService;

    /**
     * uplaod the fractions file, this is expected to be a CSV file with one header line, 3 columns
     * and multiple rows, eg:
     *   Month,Profile,Fraction
     *   JAN,A,0.3125
     * @param fractionsCsv The file should be uplaoded as form data
     * @return A json response with a fraction object per line uploaded
     */
    @RequestMapping(
            path= "/fractions",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseDTO save(@RequestParam("file") MultipartFile fractionsCsv) {
        return fractionService.save(fractionsCsv);
    }

    /**
     * delete by ID
     * @param fractionId
     * @return
     */
    @RequestMapping(
            path= "/fractions/{fraction_id}",
            method = RequestMethod.DELETE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody void delete(
            @PathVariable("fraction_id") String fractionId) {
        fractionService.delete(fractionId);
    }

    /**
     * get a fraction by ID
     * @param fractionId
     * @return
     */
    @RequestMapping(
            path= "/fractions/{fraction_id}",
            method = RequestMethod.GET,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody FractionDTO get(@PathVariable("fraction_id") String fractionId) {
        return fractionService.get(fractionId);
    }

    /**
     * update a fraction
     * @param fractionId
     * @return
     */
    @RequestMapping(
            path= "/fractions/{fraction_id}",
            method = RequestMethod.PUT,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody FractionDTO update(
            @PathVariable("fraction_id") String fractionId,
            @RequestBody @Valid FractionDTO fraction) {
        return fractionService.save(fraction, fractionId);
    }

}

package com.etpa.electric.controller;

import com.etpa.electric.dto.MetreReadingDTO;
import com.etpa.electric.dto.ResponseDTO;
import com.etpa.electric.exception.NotYetImplementedException;
import com.etpa.electric.service.MetreReadingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * only the file upload endpoint was implemented. it is unclear to me what the semantics
 * of the other endpoints would be, should they also be file based or entity level like
 * traditional CRUD endpoints?
 */
@RestController
@RequestMapping(path="/etpa")
public class MetreReadingController {
    @Autowired
    private MetreReadingService metreReadingService;

    /**
     * insert the metre readings
     * @param metreReadingsCsv
     * @return
     */
    @RequestMapping(
            path= "/metrereadings",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseDTO save(@RequestParam("file") MultipartFile metreReadingsCsv) {
        return metreReadingService.save(metreReadingsCsv);
    }

    /**
     * delete by ID
     * @param metreReadingId
     */
    @RequestMapping(
            path= "/metrereadings/{metre_reading_id}",
            method = RequestMethod.DELETE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody void delete(
            @PathVariable("metre_reading_id") String metreReadingId) {
        metreReadingService.delete(metreReadingId);
    }

    /**
     * get a reading by ID
     * @param metreReadingId
     * @return
     */
    @RequestMapping(
            path= "/metrereadings/{metre_reading_id}",
            method = RequestMethod.GET,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody MetreReadingDTO get(@PathVariable("metre_reading_id") String metreReadingId) {
        return metreReadingService.get(metreReadingId);
    }

    /**
     * update a meter reading by ID
     * @param metreReadingId
     * @param metreReading
     * @return
     */
    @RequestMapping(
            path= "/metrereadings/{metre_reading_id}",
            method = RequestMethod.PUT,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody MetreReadingDTO update(
            @PathVariable("metre_reading_id") String metreReadingId,
            @RequestBody @Valid MetreReadingDTO metreReading) {
        return metreReadingService.save(metreReading, metreReadingId);
    }

}

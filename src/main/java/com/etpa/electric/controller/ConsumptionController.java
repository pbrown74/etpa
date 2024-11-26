package com.etpa.electric.controller;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.service.consumption.ConsumptionService;
import com.etpa.electric.utils.Month;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * this class provides a partial implementation of a CRUD interface onto Consumption objects.
 */
@RestController
@RequestMapping(path="/etpa")
public class ConsumptionController {
    @Autowired
    private ConsumptionService consumptionService;

    /**
     * get a consumption
     * @param month The month of consumption required: JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC
     * @param metreId The metreId, eg. 0001
     * @return
     */
    @RequestMapping(
            path= "/consumption/{month}/{metre_id}",
            method = RequestMethod.GET,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ConsumptionDTO get(@PathVariable("month") String month,
                                    @PathVariable("metre_id") String metreId) {
        return consumptionService.get(Month.valueOf(month), metreId);
    }

    /**
     * delete by Id
     * @param consumptionId
     * @return
     */
    @RequestMapping(
            path= "/consumption/{consumption_id}",
            method = RequestMethod.DELETE)
    public @ResponseBody void delete(
            @PathVariable("consumption_id") String consumptionId) {
        consumptionService.delete(consumptionId);
    }

    /**
     * save a consumption
     * @param consumption
     * @return
     */
    @RequestMapping(
            path= "/consumption",
            method = RequestMethod.POST,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ConsumptionDTO save(@RequestBody @Valid ConsumptionDTO consumption) {
        return consumptionService.save(consumption);
    }

    /**
     * update a consumption
     * @param consumptionId
     * @param consumption
     * @return
     */
    @RequestMapping(
            path= "/consumption/{consumption_id}",
            method = RequestMethod.PUT,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ConsumptionDTO update(
            @PathVariable("consumption_id") String consumptionId,
            @RequestBody @Valid ConsumptionDTO consumption) {
        return consumptionService.save(consumption, consumptionId);
    }

}

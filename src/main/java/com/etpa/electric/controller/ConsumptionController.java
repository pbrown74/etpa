package com.etpa.electric.controller;

import com.etpa.electric.dto.ConsumptionDTO;
import com.etpa.electric.exception.NotYetImplementedException;
import com.etpa.electric.service.ConsumptionService;
import com.etpa.electric.utils.Month;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * this class provides a partial implementation of a CRUD interface onto Consumption objects.
 * we implemented the CSV upload as per the requirements. the other CRUD methods are NYI.
 * i did not tackle update/delete because if that is meant to be file based too ,it is non-trivial
 * to determine how it should work. Should the absence of a line imply a delete? should a delete
 * or an update be allowed to leave a profile fraction in an invalid state (not sum(fractions)==1)?
 * or are the update/delete methods meant to be at the individual row level rather than file based?-
 * in which case the same questions apply - it would be easy to leave the fractions in invalid state.
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
     * not implemented, here for completeness only
     * @param consumption
     * @return
     */
    @RequestMapping(
            path= "/consumption",
            method = RequestMethod.POST,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ConsumptionDTO save(@RequestBody @Valid ConsumptionDTO consumption) {
        throw new NotYetImplementedException("save");
    }

    /**
     * not implemented, here for completeness only
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
        throw new NotYetImplementedException("update");
    }

}

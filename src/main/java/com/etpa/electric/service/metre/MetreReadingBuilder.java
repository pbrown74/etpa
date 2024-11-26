package com.etpa.electric.service.metre;

import com.etpa.electric.dto.*;
import com.etpa.electric.entity.MetreReading;
import org.springframework.stereotype.Service;

/**
 * a service layer for interacting with MetreReadings, validations are located here.
 * the design is to save as many as we can and report errors for the ones which failed.
 */
@Service
public class MetreReadingBuilder {

    public MetreReading buildMetreReading(final MetreReadingDTO dto, final String id){
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

    public MetreReading buildMetreReading(final MetreReadingDTO dto){
        MetreReading mr = new MetreReading();
        mr.setMetreReading(dto.getMetreReading());
        mr.setMetreId(dto.getMetreId());
        mr.setMonth(dto.getMonth());
        mr.setProfile(dto.getProfile());
        return mr;
    }

    public MetreReadingDTO buildMetreReading(final MetreReading mr){
        return new MetreReadingDTO(mr.getId(), mr.getMetreId(), mr.getProfile(), mr.getMonth(), mr.getMetreReading());
    }

}
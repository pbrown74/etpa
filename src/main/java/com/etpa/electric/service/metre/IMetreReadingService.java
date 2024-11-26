package com.etpa.electric.service.metre;

import com.etpa.electric.dto.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * interface to manage MetreReadings
 */
public interface IMetreReadingService {

    ResponseDTO save(final MultipartFile metreReadingsCsv);

    MetreReadingDTO update(final MetreReadingDTO reading, final String id);

    void delete(final String metreReadingId);

    MetreReadingDTO get(final String metreReadingId);

}
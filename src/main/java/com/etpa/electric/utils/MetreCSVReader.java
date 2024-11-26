package com.etpa.electric.utils;

import com.etpa.electric.dto.MetreReadingDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * TODO generalise for other DTOs and make them all use the CSVReader open source lib
 */
@Service
public class MetreCSVReader {

    public List<MetreReadingDTO> read(final MultipartFile metreReadingsCsv) throws IOException {
        List<MetreReadingDTO> readingDtos = new Vector<>();
        InputStream inputStream = metreReadingsCsv.getInputStream();
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .skip(1)
                .forEach(l->{
                    readingDtos.add(newLine(l));
                });
        return readingDtos;
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

}

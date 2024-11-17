package com.etpa.electric;

import com.etpa.electric.utils.Errors;
import com.etpa.electric.utils.Month;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.etpa.electric.dto.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * The approach here is to test via the endpoints using the Spring RestTemplate.
 * No mocking is used, all the real code and backend is being tested. This is an integration test.
 * The idea of this kind of test is to make it as close to PROD as possible.
 * I would separately have unit tests around code in complex classes.
 * We build DTOs and use the Spring infrastructure to create JSON and call the endpoints over HTTP.
 * We test assertions using DTO responses.
 * The test harness should be run is isolation without needing to start the backend separately,
 * this is handled for us by TestContainers.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = ElectricApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectricApplicationTests {
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private ObjectMapper objectMapper;
    private final static String GETConsumptionUrl = "http://localhost:${server.port}/etpa/consumption/{month}/{metreId}";
    private final static String POSTFractionsUrl = "http://localhost:${server.port}/etpa/fractions";
    private final static String POSTReadingsUrl = "http://localhost:${server.port}/etpa/metrereadings";
    private final static String DELETEFractionsUrl = "http://localhost:${server.port}/etpa/fractions/{fractionId}";
    private final static String DELETEReadingsUrl = "http://localhost:${server.port}/etpa/metrereadings/{metreReadingId}";
    private final static String DELETEConsumptionUrl = "http://localhost:${server.port}/etpa/consumption/{consumptionId}";

    @Value("${server.port}")
    private String serverPort;

    public ElectricApplicationTests() {
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    /**
     * test1 files contain good data so we test the 'happy path' here
     */
    @Test
    @Order(1)
    public void insert_Fractions_And_Readings_Then_Check_Consumption() {
        // POST fractions to database
        MultiValueMap<String, Object> body = loadBody("test1_fractions.csv");
        ResponseDTO response = doPOST(POSTFractionsUrl, body, ResponseDTO.class);
        // check response body from POST (should contain Fractions we uploaded)
        Object[] items = response.getItems().toArray(new Object[]{});
        FractionDTO[] fractions = objectMapper.convertValue(items, FractionDTO[].class);
        assertTrue(fractions.length==24);
        Optional<FractionDTO> firstFraction = findFraction(fractions, Month.JAN, "A");
        assertTrue(firstFraction.isPresent());
        assertTrue(firstFraction.get().getFraction().compareTo(BigDecimal.valueOf(0.3125))==0);
        Optional<FractionDTO> lastFraction = findFraction(fractions, Month.DEC, "B");
        assertTrue(lastFraction.isPresent());
        assertTrue(lastFraction.get().getFraction().compareTo(BigDecimal.valueOf(0.0333))==0);
        // POST metre readings to database
        body = loadBody("test1_readings.csv");
        response = doPOST(POSTReadingsUrl, body, ResponseDTO.class);
        // check response body from POST (should contain Readings we uploaded)
        items = response.getItems().toArray(new Object[]{});
        MetreReadingDTO[] readings = objectMapper.convertValue(items, MetreReadingDTO[].class);
        assertTrue(readings.length==24);
        Optional<MetreReadingDTO> firstReading = findMetreReading(readings,"0001", Month.JAN);
        assertTrue(firstReading.isPresent());
        assertTrue(firstReading.get().getMetreReading().compareTo(BigDecimal.valueOf(10))==0);
        assertTrue(firstReading.get().getProfile().equals("A"));
        Optional<MetreReadingDTO> lastReading = findMetreReading(readings,"0004", Month.DEC);
        assertTrue(lastReading.isPresent());
        assertTrue(lastReading.get().getMetreReading().compareTo(BigDecimal.valueOf(30))==0);
        assertTrue(lastReading.get().getProfile().equals("B"));
        // GET consumption created as a side effect of loading the Metre Readings
        Map<String, String> params = new HashMap<>();
        params.put("metreId", "0004");
        params.put("month", Month.DEC.name());
        ConsumptionDTO consumption = doGET(GETConsumptionUrl, ConsumptionDTO.class, params);
        // check consumption value is 1.0, as per the test data
        assertTrue(consumption.getConsumption().compareTo(BigDecimal.ONE)==0);
        // cleanup after test
        cleanup(fractions, readings);
    }

    /**
     * test2_fractions has profile A misconfigured where the fractions dont sum to 1 but profile B is fine
     * @throws IOException
     */
    @Test
    @Order(2)
    public void insert_ProfileA_Fractions_Not_Sum_to_one_Then_Check_Error() {
        MultiValueMap<String, Object> body = loadBody("test2_fractions.csv");
        ResponseDTO response = doPOST(POSTFractionsUrl, body, ResponseDTO.class);
        Object[] items = response.getItems().toArray(new Object[]{});
        FractionDTO[] fractions = objectMapper.convertValue(items, FractionDTO[].class);
        assertTrue(fractions.length==12);
        List<ErrorDTO> errors = response.getErrors();
        assertTrue(errors.size()==1);
        ErrorDTO error = errors.get(0);
        assertTrue(error.getCode()==Errors.FRACTIONS_DONT_SUM_TO_ONE);
        // cleanup after test
        cleanup(fractions);
    }

    /**
     * test3 is a corrupted file, this should trigger an exception which is mapped to Http Status Code 400
     * @throws IOException
     */
    @Test
    @Order(3)
    public void insert_Corrupted_Fractions_file_Then_Check_StatusCode() {
        // POST fractions file
        MultiValueMap<String, Object> body = loadBody("test3_fractions.csv");
        HttpStatusCode sc = doPOSTVerbose(POSTFractionsUrl, body);
        assertTrue(sc.value()==400);
    }

    /**
     * test4 fractions are fine, one of the metres have a metre reading which is outside acceptable
     * tolerance given the fractions that were uploaded. the other metre should upload fine. so
     * we check for one error and one upload.
     * @throws IOException
     */
    @Test
    @Order(4)
    public void insert_Readings_outside_Tolerance_Then_Check_Errors() {
        MultiValueMap<String, Object> body = loadBody("test4_fractions.csv");
        ResponseDTO response = doPOST(POSTFractionsUrl, body, ResponseDTO.class);
        Object[] items = response.getItems().toArray(new Object[]{});
        FractionDTO[] fractions = objectMapper.convertValue(items, FractionDTO[].class);
        assertTrue(fractions.length==24);
        body = loadBody("test4_readings.csv");
        response = doPOST(POSTReadingsUrl, body, ResponseDTO.class);
        items = response.getItems().toArray(new Object[]{});
        MetreReadingDTO[] readings = objectMapper.convertValue(items, MetreReadingDTO[].class);
        assertTrue(readings.length==12);
        List<ErrorDTO> errors = response.getErrors();
        assertTrue(errors.size()==1);
        ErrorDTO error = errors.get(0);
        assertTrue(error.getCode()==Errors.INVALID_CONSUMPTION_FOR_MONTH);
        // cleanup after test
        cleanup(fractions, readings);
    }

    /**
     * test5 fractions are good, but the readings refer to a fraction C that was not uploaded
     * @throws IOException
     */
    @Test
    @Order(5)
    public void insert_Readings_for_nonexistant_FractionProfile_Then_Check_Errors() {
        MultiValueMap<String, Object> body = loadBody("test5_fractions.csv");
        ResponseDTO response = doPOST(POSTFractionsUrl, body, ResponseDTO.class);
        Object[] items = response.getItems().toArray(new Object[]{});
        FractionDTO[] fractions = objectMapper.convertValue(items, FractionDTO[].class);
        assertTrue(fractions.length==24);
        body = loadBody("test5_readings.csv");
        response = doPOST(POSTReadingsUrl, body, ResponseDTO.class);
        items = response.getItems().toArray(new Object[]{});
        MetreReadingDTO[] readings = objectMapper.convertValue(items, MetreReadingDTO[].class);
        assertTrue(readings.length==0);
        List<ErrorDTO> errors = response.getErrors();
        assertTrue(errors.size()==1);
        ErrorDTO error = errors.get(0);
        assertTrue(error.getCode()==Errors.PROFILE_NONEXISTANT);
        // cleanup after test
        cleanup(fractions, readings);
    }

    /**
     * test6 readings are not in increasing order of metre reading value, check the error.
     * this applies to only one profile, the other profile is good.
     * @throws IOException
     */
    @Test
    @Order(6)
    public void insert_Readings_out_of_order_Then_Check_Errors() {
        MultiValueMap<String, Object> body = loadBody("test6_fractions.csv");
        ResponseDTO response = doPOST(POSTFractionsUrl, body, ResponseDTO.class);
        Object[] items = response.getItems().toArray(new Object[]{});
        FractionDTO[] fractions = objectMapper.convertValue(items, FractionDTO[].class);
        assertTrue(fractions.length == 24);
        body = loadBody("test6_readings.csv");
        response = doPOST(POSTReadingsUrl, body, ResponseDTO.class);
        items = response.getItems().toArray(new Object[]{});
        MetreReadingDTO[] readings = objectMapper.convertValue(items, MetreReadingDTO[].class);
        assertTrue(readings.length == 12);
        List<ErrorDTO> errors = response.getErrors();
        assertTrue(errors.size() == 1);
        ErrorDTO error = errors.get(0);
        assertTrue(error.getCode()==Errors.FILE_MONTHS_DECREMENT_FOR_METRE);
        // cleanup after test
        cleanup(fractions, readings);
    }

    /**
     * test7 readings are for 3 metres. the middle one has a tolerance issue.
     * we expect to find the other two only in the saved readings.
     * @throws IOException
     */
    @Test
    @Order(7)
    public void insert_Three_Readings_whith_one_faulty_Then_Check_Two_Metres_Saved() {
        MultiValueMap<String, Object> body = loadBody("test7_fractions.csv");
        ResponseDTO response = doPOST(POSTFractionsUrl, body, ResponseDTO.class);
        Object[] items = response.getItems().toArray(new Object[]{});
        FractionDTO[] fractions = objectMapper.convertValue(items, FractionDTO[].class);
        assertTrue(fractions.length == 24);
        body = loadBody("test7_readings.csv");
        response = doPOST(POSTReadingsUrl, body, ResponseDTO.class);
        items = response.getItems().toArray(new Object[]{});
        MetreReadingDTO[] readings = objectMapper.convertValue(items, MetreReadingDTO[].class);
        assertTrue(readings.length == 24);
        Arrays.asList(readings).stream().forEach(r->assertTrue(!r.getMetreId().equals("0004")));
        List<ErrorDTO> errors = response.getErrors();
        assertTrue(errors.size() == 1);
        ErrorDTO error = errors.get(0);
        assertTrue(error.getCode()==Errors.INVALID_CONSUMPTION_FOR_MONTH);
        // cleanup after test
        cleanup(fractions, readings);
    }

    /**
     * helpers below
     */

    private MultiValueMap<String, Object> loadBody(String testFileName) {
        FileSystemResource fractions = new FileSystemResource("src/test/resources/"+testFileName);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fractions);
        return body;
    }

    private void doDELETE(String url, Map<String, String> urlParams) {
        restTemplate.delete(expandPort(url), urlParams);
    }

    private <Output> Output doPOST(String url, MultiValueMap<String, Object> body, Class<Output> clazz) {
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        ResponseEntity<Output> output = restTemplate.postForEntity(
                expandPort(url),
                requestEntity,
                clazz);
        return output.getBody();
    }

    private HttpStatusCode doPOSTVerbose(String url, MultiValueMap<String, Object> body) {
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        ResponseEntity<String> out;
        try {
            out = restTemplate.exchange(expandPort(url),
                    HttpMethod.POST, requestEntity, String.class);
        } catch (HttpStatusCodeException e) {
            return e.getStatusCode();
        }
        return out.getStatusCode();
    }

    private <Output> Output doGET(String url, Class<Output> clazz, Map<String, String> urlParams) {
        Output output = restTemplate.getForObject(
                expandPort(url),
                clazz,
                urlParams);
        return output;
    }

    private String expandPort(String url) {
        return url.replace("${server.port}", this.serverPort);
    }

    private Optional<FractionDTO> findFraction(FractionDTO[] fractions, Month month, String profile){
        for(FractionDTO dto : fractions){
            if(dto.getMonth()==month && dto.getProfile().equals(profile)){
                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }

    private Optional<MetreReadingDTO> findMetreReading(MetreReadingDTO[] readings, String metreId, Month month){
        for(MetreReadingDTO dto : readings){
            if(dto.getMonth()==month && dto.getMetreId().equals(metreId)){
                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }

    private void cleanup(FractionDTO[] fractions){
        cleanup(fractions, null);
    }

    private void cleanup(FractionDTO[] fractions, MetreReadingDTO[] readings){
        // cleanup the database in a quick way without taking it down completely between tests
        if(readings!=null){
            Arrays.asList(readings).stream().forEach(r->{
                // load the consumption created for the reading and delete it by ID
                Map<String, String> p = new HashMap<>();
                p.put("metreId", r.getMetreId());
                p.put("month", r.getMonth().name());
                ConsumptionDTO c = doGET(GETConsumptionUrl, ConsumptionDTO.class, p);
                doDELETE(DELETEConsumptionUrl, Collections.singletonMap("consumptionId", c.getId()));
                // remove the readings
                doDELETE(DELETEReadingsUrl, Collections.singletonMap("metreReadingId", r.getId()));
            });
        }
        // remove the fractions
        Arrays.asList(fractions).stream().forEach(f->doDELETE(DELETEFractionsUrl, Collections.singletonMap("fractionId", f.getId())));
    }

}

package com.etpa.electric.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * note that per row validation errors are treated as HTTP 200, a validation error code is returned in the json.
 * this class is a Spring hook used to map exceptions to HTTP response codes in one place for all the service layers.
 */

@ControllerAdvice
class GlobalControllerExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadFileUploadException.class)
    public ResponseEntity<String> handleBadFileUpload(BadFileUploadException ex) {
        logger.error("Bad file upload: "+ ex.getFile());
        return new ResponseEntity<>(ex.getFile(), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ConsumptionNotFoundException.class)
    public ResponseEntity<String> handleConsumptionNotFoundException(ConsumptionNotFoundException ex) {
        logger.error("No Consumption found for MetreId and Month: "+ ex.getConsumptionKey());
        return new ResponseEntity<>(ex.getConsumptionKey(), HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ExceptionHandler(NotYetImplementedException.class)
    public ResponseEntity<String> handleNotYetImplementedException(NotYetImplementedException ex) {
        return new ResponseEntity<>("Feature not implemented: " + ex.getFeature(), HttpStatus.NOT_IMPLEMENTED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>("Illegal parameter: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
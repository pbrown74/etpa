package com.etpa.electric.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If a fraction/reading file partially uploads, there are errors in the response
 * for validation failures. this is the error DTO.
 */
public class ErrorDTO implements Cloneable {

    private String message;
    private Integer code;

    @JsonCreator()
    public ErrorDTO(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

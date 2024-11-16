package com.etpa.electric.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * since when doing a file load, some lines can work and some fail .. we respond using a structure
 * which allows us to return both the uploaded DTOs and the errors for those which failed upload.
 * the HTTP code will always be 200 if we can generate a Response. Inside ErrorDTO there are application
 * level errors to know what validation failed exactly.
 */
public class ResponseDTO<T> implements Cloneable {

    private List<T> items;
    private List<ErrorDTO> errors;

    public ResponseDTO(List<T> items, List<ErrorDTO> errors){
        this.items = items;
        this.errors = errors;
    }

    @JsonProperty("items")
    public List<T> getItems() {
        return items;
    }

    @JsonProperty("errors")
    public List<ErrorDTO> getErrors() {
        return errors;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

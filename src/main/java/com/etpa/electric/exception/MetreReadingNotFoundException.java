package com.etpa.electric.exception;

public class MetreReadingNotFoundException extends RuntimeException {

    private String fraction;

    public MetreReadingNotFoundException(String fraction){
        this.fraction = fraction;
    }

    public String getKey(){
        return this.fraction;
    }
}

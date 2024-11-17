package com.etpa.electric.exception;

public class FractionNotFoundException extends RuntimeException {

    private String fraction;

    public FractionNotFoundException(String fraction){
        this.fraction = fraction;
    }

    public String getKey(){
        return this.fraction;
    }
}

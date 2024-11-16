package com.etpa.electric.exception;

public class NotYetImplementedException extends RuntimeException {

    private String feature;

    public NotYetImplementedException(String feature){
        this.feature = feature;
    }

    public String getFeature(){
        return feature;
    }
}

package com.etpa.electric.exception;

import com.etpa.electric.utils.Month;

public class ConsumptionNotFoundException extends RuntimeException {

    private String consumption;

    public ConsumptionNotFoundException(String metreId, Month month){
        this.consumption = metreId + "/" + month.name();
    }

    public ConsumptionNotFoundException(String id){
        this.consumption = id;
    }

    public String getConsumptionKey(){
        return this.consumption;
    }
}

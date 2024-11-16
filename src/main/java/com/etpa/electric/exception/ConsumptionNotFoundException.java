package com.etpa.electric.exception;

import com.etpa.electric.utils.Month;

public class ConsumptionNotFoundException extends RuntimeException {

    private String metreId;

    private Month month;

    public ConsumptionNotFoundException(String metreId, Month month){
        this.metreId = metreId;
        this.month = month;
    }

    public String getConsumptionKey(){
        return this.metreId + "/" + this.month.name();
    }
}

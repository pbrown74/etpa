package com.etpa.electric.exception;

public class ValidationException extends RuntimeException {

    private int code;
    private String message;

    public ValidationException(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}

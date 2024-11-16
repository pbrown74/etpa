package com.etpa.electric.exception;

public class BadFileUploadException extends RuntimeException {

    private String file;

    public BadFileUploadException(String file){
        this.file = file;
    }

    public String getFile(){
        return file;
    }
}

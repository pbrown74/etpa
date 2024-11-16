package com.etpa.electric.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class ItemDTO<T> implements Cloneable {

    private T item;

    @JsonCreator()
    public ItemDTO(T object){
        this.item = object;
    }

    @JsonUnwrapped
    public T getItem() {
        return item;
    }

}

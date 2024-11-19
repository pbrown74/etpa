package com.etpa.electric.utils;

public enum Month {
    JAN(1), FEB(2), MAR(3), APR(4), MAY(5), JUN(6), JUL(7), AUG(8), SEP(9), OCT(10), NOV(11), DEC(12);

    private Integer code;

    Month(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static Month valueOf(int i){
        for (Month as : values()){
            if (as.code == i){
                return as;
            }
        }
        throw new IllegalArgumentException("Month has no matching constant for " + i);
    }

    public static Month from(String s){
        for (Month m : values()){
            if (m.name().equals(s)){
                return m;
            }
        }
        throw new IllegalArgumentException("No matching constant for " + s);
    }

    public static Month prev(Month m){
        if(m.getCode()==1){
            return valueOf(12);
        }
        return valueOf(m.code-1);
    }

}
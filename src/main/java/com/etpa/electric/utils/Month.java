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

    public static Month prev(Month m){
        if(m.getCode()==1){
            throw new IllegalArgumentException("No Month before: "+
                    Month.valueOf(m.getCode()));
        }
        return valueOf(m.code-1);
    }

}
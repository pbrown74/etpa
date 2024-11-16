package com.etpa.electric.utils;

/**
 * these application level error codes appear in the JSON response for an error
 */
public interface Errors {
    int MONTH_FORMAT_WRONG = 1;
    int FILE_METRE_READER_ISSUE = 2;
    int DB_METRE_READER_ISSUE = 3;
    int FILE_MONTHS_DECREMENT_FOR_METRE = 4;
    int FRACTIONS_DONT_SUM_TO_ONE = 5;
    int FILE_FRACTIONS_ISSUE = 6;
    int PROFILE_NONEXISTANT = 7;
    int DB_FRACTION_ISSUE = 8;
    int MISSING_PREV_READING = 9;
    int INVALID_CONSUMPTION_FOR_MONTH = 10;
}

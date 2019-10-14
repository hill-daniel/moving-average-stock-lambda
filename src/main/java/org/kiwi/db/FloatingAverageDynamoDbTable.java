package org.kiwi.db;

public class FloatingAverageDynamoDbTable {

    static final String TABLE_NAME = "floating_average";
    static final String FLOATING_AVERAGE = "floatingAverage";
    static final String CLOSING_QUOTE = "closingQuote";
    static final String LAST_AVERAGE = "lastAverage";
    static final String HASH_KEY_NAME = "floatId";
    static final String RANGE_KEY_NAME = "rangeIndex";
    static final String STOCK_STATE = "currentState";
    static final String CLOSING_DATE = "closingDate";
    public static final int MAX_RANGE_INDEX = 199;
}

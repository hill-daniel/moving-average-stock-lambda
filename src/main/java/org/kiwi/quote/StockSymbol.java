package org.kiwi.quote;

public enum StockSymbol {

    DAX("^GDAXI");

    private final String symbol;

    StockSymbol(String yahooSymbol) {
        this.symbol = yahooSymbol;
    }

    public String getSymbol() {
        return symbol;
    }
}

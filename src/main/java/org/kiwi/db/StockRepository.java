package org.kiwi.db;

import org.kiwi.quote.StockSymbol;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import static java.math.BigDecimal.ZERO;

public class StockRepository {

    public BigDecimal getLastClosingQuoteOf(StockSymbol stockSymbol) {
        return retrieveStock(stockSymbol)
                .map(Stock::getQuote)
                .map(StockQuote::getPreviousClose)
                .orElse(ZERO);
    }

    Optional<Stock> retrieveStock(StockSymbol stockSymbol) {
        try {
            Stock stock = YahooFinance.get(stockSymbol.getSymbol());
            return Optional.ofNullable(stock);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve Stock with Yahoo symbol [" + stockSymbol.getSymbol() + "]", e);
        }
    }
}

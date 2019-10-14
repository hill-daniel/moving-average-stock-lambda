package org.kiwi.db;


import org.junit.Test;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class StockRepositoryIntegrationTest {

    @Test
    public void should_retrieve_last_closing_quote_of_stock() throws Exception {
        StockRepository stockRepository = new StockRepository();

        BigDecimal closingQuote = stockRepository.getLastClosingQuoteOf(StockSymbol.DAX);

        System.out.println("Closing quote: " + closingQuote.toPlainString());

        assertThat(closingQuote).isNotNull();
        assertThat(closingQuote).isNotEqualTo(BigDecimal.ZERO);
    }
}
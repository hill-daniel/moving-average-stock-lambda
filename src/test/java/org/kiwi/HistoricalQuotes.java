package org.kiwi;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import yahoofinance.histquotes.HistoricalQuote;

import static java.util.stream.Collectors.toList;

public class HistoricalQuotes {

    private List<HistoricalQuote> createHistoricalQuotes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/HistoricalQuotes.json");
        return objectMapper.readValue(resourceAsStream, new TypeReference<List<HistoricalQuote>>() {
        });
    }

    public Collection<BigDecimal> getClosingQuotes() throws IOException {
        List<HistoricalQuote> historicalQuotes = createHistoricalQuotes();
        return historicalQuotes.stream()
                .map(HistoricalQuote::getClose)
                .collect(toList());
    }

    public Collection<Item> getAsItems() throws IOException {
        AtomicInteger atomicInteger = new AtomicInteger();
        List<HistoricalQuote> historicalQuotes = createHistoricalQuotes();
        return historicalQuotes.stream()
                .map(HistoricalQuote::getClose)
                .map(closingQuote -> new Item()
                        .withPrimaryKey(new PrimaryKey("floatId", "floatingAverage:DAX"))
                        .withInt("rangeIndex", atomicInteger.getAndIncrement())
                        .withNumber("closingQuote", closingQuote))
                .collect(toList());
    }
}

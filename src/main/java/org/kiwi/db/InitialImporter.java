package org.kiwi.db;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;

import org.kiwi.FloatingAverageLambdaModule;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

import static java.util.stream.Collectors.toList;
import static org.kiwi.db.FloatingAverageDynamoDbTable.CLOSING_DATE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.CLOSING_QUOTE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.FLOATING_AVERAGE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.HASH_KEY_NAME;
import static org.kiwi.db.FloatingAverageDynamoDbTable.LAST_AVERAGE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.RANGE_KEY_NAME;
import static org.kiwi.db.FloatingAverageDynamoDbTable.TABLE_NAME;
import static org.kiwi.quote.StockSymbol.DAX;
import static yahoofinance.histquotes.Interval.DAILY;


public class InitialImporter {

    private static final DateTimeFormatter CLOSING_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Injector INJECTOR = Guice.createInjector(new FloatingAverageLambdaModule());

    public static void main(String[] args) {
        StockRepository stockRepository = INJECTOR.getInstance(StockRepository.class);
        Optional<Stock> stockOptional = stockRepository.retrieveStock(DAX);
        stockOptional.ifPresent(InitialImporter::writeQuotes);
    }

    private static void writeQuotes(Stock stock) {
        try {
            List<HistoricalQuote> historicalQuotes = getHistoricalQuotes(stock);
            List<Item> items = createItemsFrom(historicalQuotes);
            writeToDynamo(items);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write items to DynamoDB", e);
        }
    }

    private static List<HistoricalQuote> getHistoricalQuotes(Stock stock) throws IOException {
        LocalDateTime today = LocalDateTime.now();
        GregorianCalendar from = new GregorianCalendar(2016, 1, 1);
        GregorianCalendar to = new GregorianCalendar(today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        List<HistoricalQuote> historicalQuotes = stock.getHistory(from, to, DAILY);
        return historicalQuotes.stream()
                .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                .collect(toList());
    }

    private static List<Item> createItemsFrom(List<HistoricalQuote> sortedQuotes) {
        AtomicInteger atomicInteger = new AtomicInteger(200);
        return sortedQuotes.stream()
                .map(toItemWithRangeKeyOf(atomicInteger))
                .collect(toList());
    }

    private static Function<HistoricalQuote, Item> toItemWithRangeKeyOf(AtomicInteger atomicInteger) {
        return historicalQuote -> new Item()
                .withPrimaryKey(new PrimaryKey(HASH_KEY_NAME, FLOATING_AVERAGE + ":" + DAX.name()))
                .withInt(RANGE_KEY_NAME, atomicInteger.decrementAndGet())
                .withNumber(CLOSING_QUOTE, historicalQuote.getClose())
                .withNumber(LAST_AVERAGE, 0)
                .withString(CLOSING_DATE, CLOSING_DATE_FORMATTER.format(toInstant(historicalQuote)));
    }

    private static ZonedDateTime toInstant(HistoricalQuote historicalQuote) {
        return ZonedDateTime.ofInstant(historicalQuote.getDate().toInstant(), ZoneOffset.UTC);
    }

    private static void writeToDynamo(List<Item> items) {
        ItemRepository itemRepository = INJECTOR.getInstance(ItemRepository.class);
        items.stream()
                .limit(200)
                .forEach(item -> itemRepository.put(TABLE_NAME, item));
    }
}

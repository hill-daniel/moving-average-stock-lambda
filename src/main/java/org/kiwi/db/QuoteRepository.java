package org.kiwi.db;

import com.google.inject.Inject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import org.kiwi.model.CalculationData;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.kiwi.db.FloatingAverageDynamoDbTable.CLOSING_DATE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.FLOATING_AVERAGE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.HASH_KEY_NAME;
import static org.kiwi.db.FloatingAverageDynamoDbTable.LAST_AVERAGE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.MAX_RANGE_INDEX;
import static org.kiwi.db.FloatingAverageDynamoDbTable.RANGE_KEY_NAME;
import static org.kiwi.db.FloatingAverageDynamoDbTable.CLOSING_QUOTE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.TABLE_NAME;

public class QuoteRepository {

    private static final DateTimeFormatter CLOSING_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ItemRepository itemRepository;
    private final Clock clock;

    @Inject
    QuoteRepository(ItemRepository itemRepository, Clock clock) {
        this.itemRepository = itemRepository;
        this.clock = clock;
    }

    public void write(CalculationData calculationData, StockState currentState) {
        Item closingQuoteItem = createClosingQuoteItem(calculationData, currentState.getCurrentTableIndex());
        itemRepository.put(TABLE_NAME, closingQuoteItem);
    }

    public Collection<BigDecimal> retrieveFor(StockSymbol symbol) {
        Collection<Item> quoteItems = retrieveQuoteItemsFor(symbol);
        return quoteItems.stream()
                .map(quoteItem -> quoteItem.getNumber(CLOSING_QUOTE))
                .collect(toList());
    }

    private Item createClosingQuoteItem(CalculationData calculationData, int currentTableIndex) {
        return new Item()
                .withPrimaryKey(new PrimaryKey(HASH_KEY_NAME, FLOATING_AVERAGE + ":" + calculationData.getStockSymbol().name()))
                .withInt(RANGE_KEY_NAME, currentTableIndex)
                .withNumber(CLOSING_QUOTE, calculationData.getClosingQuote())
                .withNumber(LAST_AVERAGE, calculationData.getFloatingAverage())
                .withString(CLOSING_DATE, CLOSING_DATE_FORMATTER.format(now(clock).minusDays(1)));
    }

    private Collection<Item> retrieveQuoteItemsFor(StockSymbol symbol) {
        QuerySpec query = createQuoteQueryFor(symbol.name());
        return itemRepository.getWithQuery(TABLE_NAME, query);
    }

    private QuerySpec createQuoteQueryFor(String stockId) {
        return new QuerySpec()
                .withKeyConditionExpression(HASH_KEY_NAME + " = :floatId and " + RANGE_KEY_NAME + " between :from and :to")
                .withValueMap(new ValueMap()
                        .withString(":floatId", FLOATING_AVERAGE + ":" + stockId)
                        .withInt(":from", 0)
                        .withInt(":to", MAX_RANGE_INDEX));
    }
}

package org.kiwi.db;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.HistoricalQuotes;
import org.kiwi.model.CalculationData;
import org.kiwi.model.ImmutableCalculationData;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;

import static java.math.BigDecimal.ZERO;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.db.FloatingAverageDynamoDbTable.TABLE_NAME;
import static org.kiwi.quote.StockSymbol.DAX;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class QuoteRepositoryTest {

    private ItemRepository itemRepository;
    private QuoteRepository quoteRepository;
    private HistoricalQuotes historicalQuotes;

    @Before
    public void setUp() throws Exception {
        itemRepository = mock(ItemRepository.class);
        Clock clock = Clock.fixed(ZonedDateTime.of(2016, 11, 14, 15, 55, 33, 22, UTC).toInstant(), UTC);
        quoteRepository = new QuoteRepository(itemRepository, clock);
        historicalQuotes = new HistoricalQuotes();
    }

    @Test
    public void should_write_closing_quote_and_last_average() throws Exception {
        StockState currentState = org.kiwi.db.ImmutableStockState.builder()
                .currentTableIndex(20)
                .alertState(StockState.AlertState.NONE)
                .build();
        BigDecimal closingQuote = new BigDecimal("10616.00");
        BigDecimal lastAverage = new BigDecimal("10163.73");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(closingQuote)
                .floatingAverage(lastAverage)
                .percentageThreshold(ZERO)
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.NONE)
                .build();

        this.quoteRepository.write(calculationData, currentState);

        Item expectedFloatingAverageItem = new Item()
                .withPrimaryKey(new PrimaryKey("floatId", "floatingAverage:DAX"))
                .withInt("rangeIndex", 20)
                .withNumber("closingQuote", closingQuote)
                .withNumber("lastAverage", lastAverage)
                .withString("closingDate", "2016-11-13");
        verify(itemRepository).put("floating_average", expectedFloatingAverageItem);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    public void should_retrieve_and_map_items_to_big_decimal() throws Exception {
        Collection<Item> historicalQuoteItems = historicalQuotes.getAsItems();
        when(itemRepository.getWithQuery(eq(TABLE_NAME), any(QuerySpec.class))).thenReturn(historicalQuoteItems);

        Collection<BigDecimal> quotes = quoteRepository.retrieveFor(DAX);

        assertThat(quotes.size()).isEqualTo(200);
    }
}
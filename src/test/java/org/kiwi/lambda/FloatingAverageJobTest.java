package org.kiwi.lambda;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.HistoricalQuotes;
import org.kiwi.alert.DeviationThresholdAlert;
import org.kiwi.db.ImmutableStockState;
import org.kiwi.db.QuoteRepository;
import org.kiwi.db.StateRepository;
import org.kiwi.db.StockRepository;
import org.kiwi.db.StockState;
import org.kiwi.model.CalculationData;
import org.kiwi.model.ImmutableCalculationData;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;

import static org.kiwi.db.StockState.AlertState.NONE;
import static org.kiwi.quote.StockSymbol.DAX;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FloatingAverageJobTest {

    private static final String DEVIATION_THRESHOLD = "5.0";

    private StateRepository stateRepository;
    private StockRepository stockRepository;
    private QuoteRepository quoteRepository;
    private DeviationThresholdAlert deviationThresholdAlert;
    private FloatingAverageJob floatingAverageJob;
    private HistoricalQuotes historicalQuotes;

    @Before
    public void setUp() throws Exception {
        stateRepository = mock(StateRepository.class);
        stockRepository = mock(StockRepository.class);
        quoteRepository = mock(QuoteRepository.class);
        deviationThresholdAlert = mock(DeviationThresholdAlert.class);
        floatingAverageJob = new FloatingAverageJob(stateRepository, stockRepository, quoteRepository, deviationThresholdAlert, DEVIATION_THRESHOLD);
        historicalQuotes = new HistoricalQuotes();
    }

    @Test
    public void should_write_closing_quote_and_floating_average_and_increment_table_index() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10765.89");
        when(stockRepository.getLastClosingQuoteOf(DAX)).thenReturn(currentStockValue);
        when(stateRepository.retrieveStateFor(DAX)).thenReturn(ImmutableStockState.builder()
                .currentTableIndex(41)
                .alertState(NONE)
                .build());
        when(quoteRepository.retrieveFor(DAX)).thenReturn(historicalQuotes.getClosingQuotes());
        BigDecimal expectedAverage = new BigDecimal("10019.997793");
        CalculationData expectedCalculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(expectedAverage)
                .percentageThreshold(new BigDecimal(DEVIATION_THRESHOLD))
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.NONE)
                .build();
        when(deviationThresholdAlert.alertAtThresholdExcessOf(expectedCalculationData)).thenReturn(NONE);

        floatingAverageJob.run();

        StockState expectedState = ImmutableStockState.builder()
                .currentTableIndex(42)
                .alertState(NONE).build();
        verify(stockRepository).getLastClosingQuoteOf(DAX);
        verify(quoteRepository).write(expectedCalculationData, expectedState);
        verify(deviationThresholdAlert).alertAtThresholdExcessOf(expectedCalculationData);
        verify(stateRepository).persist(expectedState, DAX);
    }


    @Test
    public void should_return_table_index_zero_if_max_value_is_reached() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10765.89");
        when(stockRepository.getLastClosingQuoteOf(DAX)).thenReturn(currentStockValue);
        when(stateRepository.retrieveStateFor(DAX)).thenReturn(ImmutableStockState.builder()
                .currentTableIndex(199)
                .alertState(NONE)
                .build());
        when(quoteRepository.retrieveFor(DAX)).thenReturn(historicalQuotes.getClosingQuotes());
        BigDecimal expectedAverage = new BigDecimal("10019.997793");
        CalculationData expectedCalculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(expectedAverage)
                .percentageThreshold(new BigDecimal(DEVIATION_THRESHOLD))
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.NONE)
                .build();
        when(deviationThresholdAlert.alertAtThresholdExcessOf(expectedCalculationData)).thenReturn(NONE);

        floatingAverageJob.run();

        StockState expectedState = ImmutableStockState.builder()
                .currentTableIndex(0)
                .alertState(NONE).build();
        verify(stockRepository).getLastClosingQuoteOf(DAX);
        verify(quoteRepository).write(expectedCalculationData, expectedState);
        verify(deviationThresholdAlert).alertAtThresholdExcessOf(expectedCalculationData);
        verify(stateRepository).persist(expectedState, DAX);
    }
}
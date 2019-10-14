package org.kiwi.lambda;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.apache.log4j.Logger;
import org.kiwi.alert.DeviationThresholdAlert;
import org.kiwi.db.FloatingAverageDynamoDbTable;
import org.kiwi.db.ImmutableStockState;
import org.kiwi.db.QuoteRepository;
import org.kiwi.db.StateRepository;
import org.kiwi.db.StockRepository;
import org.kiwi.db.StockState;
import org.kiwi.db.StockState.AlertState;
import org.kiwi.model.CalculationData;
import org.kiwi.model.ImmutableCalculationData;
import org.kiwi.quote.Average;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;
import java.util.Collection;

import static org.kiwi.quote.StockSymbol.DAX;

class FloatingAverageJob {

    private final Logger LOGGER = Logger.getLogger(FloatingAverageJob.class);

    private final StateRepository stateRepository;
    private final StockRepository stockRepository;
    private final QuoteRepository quoteRepository;
    private final DeviationThresholdAlert deviationThresholdAlert;
    private final BigDecimal deviationThreshold;

    @Inject
    FloatingAverageJob(StateRepository stateRepository, StockRepository stockRepository, QuoteRepository quoteRepository, DeviationThresholdAlert deviationThresholdAlert, @Named("quote.deviation.threshold") String deviationThreshold) {
        this.stateRepository = stateRepository;
        this.stockRepository = stockRepository;
        this.quoteRepository = quoteRepository;
        this.deviationThresholdAlert = deviationThresholdAlert;
        this.deviationThreshold = new BigDecimal(deviationThreshold);
    }

    void run() {
        for (StockSymbol stock : StockSymbol.values()) {
            try {
                calculateAndPersistFor(stock);
            } catch (Exception e) {
                LOGGER.error("Failed to run FloatingAverageJob", e);
            }
        }
    }

    private void calculateAndPersistFor(StockSymbol stock) {
        ImmutableStockState currentState = getStockStateAndIncrementTableIndexFor(stock);
        CalculationData calculationData = calculateFloatingAverage(stock, currentState.getAlertState());
        quoteRepository.write(calculationData, currentState);
        AlertState newAlertState = deviationThresholdAlert.alertAtThresholdExcessOf(calculationData);
        stateRepository.persist(currentState.withAlertState(newAlertState), DAX);
    }

    private ImmutableStockState getStockStateAndIncrementTableIndexFor(StockSymbol stock) {
        StockState stockState = stateRepository.retrieveStateFor(stock);
        boolean maxTableIndexReached = stockState.getCurrentTableIndex() >= FloatingAverageDynamoDbTable.MAX_RANGE_INDEX;
        int nextTableIndex = maxTableIndexReached ? 0 : stockState.getCurrentTableIndex() + 1;
        return ImmutableStockState.copyOf(stockState)
                .withCurrentTableIndex(nextTableIndex);
    }

    private CalculationData calculateFloatingAverage(StockSymbol stock, AlertState alertState) {
        BigDecimal closingQuote = stockRepository.getLastClosingQuoteOf(stock);
        Collection<BigDecimal> quotes = quoteRepository.retrieveFor(stock);
        BigDecimal floatingAverage = new Average().calculateFor(quotes);
        LOGGER.info("Current quote of " + stock.name() + " is: " + closingQuote.toPlainString() +
                " Calculated a floating average of: " + floatingAverage.toPlainString());
        return ImmutableCalculationData.builder()
                .closingQuote(closingQuote)
                .floatingAverage(floatingAverage)
                .percentageThreshold(deviationThreshold)
                .stockSymbol(stock)
                .alertState(alertState)
                .build();
    }
}

package org.kiwi.model;

import org.immutables.value.Value;
import org.kiwi.db.StockState.AlertState;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;

@Value.Immutable
public interface CalculationData {

    BigDecimal getClosingQuote();

    BigDecimal getFloatingAverage();

    BigDecimal getPercentageThreshold();

    StockSymbol getStockSymbol();

    AlertState getAlertState();
}

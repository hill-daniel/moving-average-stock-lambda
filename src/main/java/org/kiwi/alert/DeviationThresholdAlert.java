package org.kiwi.alert;

import com.google.inject.Inject;

import org.kiwi.model.CalculationData;
import org.kiwi.db.StockState.AlertState;

import java.math.BigDecimal;

import static java.math.RoundingMode.CEILING;
import static org.kiwi.db.StockState.AlertState.BUY;
import static org.kiwi.db.StockState.AlertState.SELL;

public class DeviationThresholdAlert {

    private final static BigDecimal HUNDRED = new BigDecimal("100");

    private final DeviationAlert deviationAlert;

    @Inject
    DeviationThresholdAlert(DeviationAlert deviationAlert) {
        this.deviationAlert = deviationAlert;
    }

    public AlertState alertAtThresholdExcessOf(CalculationData calculationData) {
        BigDecimal deviation = calculateDeviationWith(calculationData);
        if (isThresholdExceeded(deviation, calculationData.getPercentageThreshold())) {
            AlertState newState = isStockValueHigherThanAverage(calculationData) ? BUY : SELL;
            if (newState != calculationData.getAlertState()) {
                deviationAlert.alert(calculationData);
                return newState;
            }
        }
        return calculationData.getAlertState();
    }

    private BigDecimal calculateDeviationWith(CalculationData calculationData) {
        return calculationData.getFloatingAverage()
                .multiply(HUNDRED)
                .divide(calculationData.getClosingQuote(), CEILING)
                .subtract(HUNDRED)
                .abs();
    }

    private boolean isThresholdExceeded(BigDecimal deviation, BigDecimal percentageThreshold) {
        return deviation.compareTo(percentageThreshold) >= 0;
    }

    private boolean isStockValueHigherThanAverage(CalculationData calculationData) {
        return calculationData.getClosingQuote().compareTo(calculationData.getFloatingAverage()) == 1;
    }
}

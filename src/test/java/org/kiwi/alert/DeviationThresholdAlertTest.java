package org.kiwi.alert;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.db.StockState;
import org.kiwi.db.StockState.AlertState;
import org.kiwi.model.CalculationData;
import org.kiwi.model.ImmutableCalculationData;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.db.StockState.AlertState.BUY;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public class DeviationThresholdAlertTest {

    private TestAlert testAlert;
    private BigDecimal percentageThreshold;
    private DeviationThresholdAlert deviationThresholdAlert;

    @Before
    public void setUp() throws Exception {
        percentageThreshold = new BigDecimal("5.00");
        testAlert = spy(new TestAlert());
        deviationThresholdAlert = new DeviationThresholdAlert(testAlert);
    }

    @Test
    public void should_not_alert_if_current_state_is_same_as_new_calculation() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10765.89");
        BigDecimal currentAverage = new BigDecimal("10227.5955");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(currentAverage)
                .percentageThreshold(percentageThreshold)
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.BUY)
                .build();

        AlertState alertState = deviationThresholdAlert.alertAtThresholdExcessOf(calculationData);

        assertThat(alertState).isEqualTo(BUY);
        verifyNoMoreInteractions(testAlert);
    }

    @Test
    public void should_not_call_alert_if_deviation_is_within_threshold() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10765.89");
        BigDecimal currentAverage = new BigDecimal("10527.5955");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(currentAverage)
                .percentageThreshold(percentageThreshold)
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.NONE)
                .build();

        AlertState alertState = deviationThresholdAlert.alertAtThresholdExcessOf(calculationData);

        assertThat(alertState).isEqualTo(calculationData.getAlertState());
        verifyNoMoreInteractions(testAlert);
    }

    @Test
    public void should_call_alert_if_current_stock_value_diverges_more_than_given_threshold() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10765.89");
        BigDecimal currentAverage = new BigDecimal("10227.5955");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(currentAverage)
                .percentageThreshold(percentageThreshold)
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.NONE)
                .build();

        AlertState alertState = deviationThresholdAlert.alertAtThresholdExcessOf(calculationData);

        assertThat(alertState).isEqualTo(AlertState.BUY);
        verify(testAlert).alert(calculationData);
    }

    @Test
    public void should_call_alert_if_current_stock_value_diverges_less_than_given_threshold() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10227.5955");
        BigDecimal currentAverage = new BigDecimal("10765.89");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(currentAverage)
                .percentageThreshold(percentageThreshold)
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.NONE)
                .build();

        AlertState alertState = deviationThresholdAlert.alertAtThresholdExcessOf(calculationData);

        assertThat(alertState).isEqualTo(AlertState.SELL);
        verify(testAlert).alert(calculationData);
    }

    @Test
    public void should_call_alert_if_state_changes_and_threshold_is_exceeded() throws Exception {
        BigDecimal currentStockValue = new BigDecimal("10227.5955");
        BigDecimal currentAverage = new BigDecimal("10765.89");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(currentStockValue)
                .floatingAverage(currentAverage)
                .percentageThreshold(percentageThreshold)
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.BUY)
                .build();

        AlertState alertState = deviationThresholdAlert.alertAtThresholdExcessOf(calculationData);

        assertThat(alertState).isEqualTo(AlertState.SELL);
        verify(testAlert).alert(calculationData);
    }

    private class TestAlert implements DeviationAlert {

        @Override
        public void alert(CalculationData calculationData) {
        }
    }
}
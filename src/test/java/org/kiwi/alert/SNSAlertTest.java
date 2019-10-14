package org.kiwi.alert;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.db.StockState;
import org.kiwi.model.CalculationData;
import org.kiwi.model.ImmutableCalculationData;
import org.kiwi.quote.StockSymbol;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SNSAlertTest {

    private static final String TOPIC_ARN = "rn:aws:sns:eu-central-1:12345:example";

    private AmazonSNSClient snsClient;
    private SNSAlert snsAlert;

    @Before
    public void setUp() throws Exception {
        snsClient = mock(AmazonSNSClient.class);
        snsAlert = new SNSAlert(snsClient, TOPIC_ARN);
    }

    @Test
    public void should_recommend_buy_if_stock_value_is_higher_than_average() throws Exception {
        BigDecimal stockValue = new BigDecimal("1234.55");
        BigDecimal floatingAverage = new BigDecimal("1134.23");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(stockValue)
                .floatingAverage(floatingAverage)
                .percentageThreshold(new BigDecimal("5"))
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.BUY)
                .build();

        snsAlert.alert(calculationData);

        String expectedMessage = "Recommendation: buy!\nThe current closing value 1234.55 of observed stock DAX " +
                "deviates more than 5 percent from the current floating average of 1134.23.";
        PublishRequest expectedPublishRequest = new PublishRequest()
                .withTopicArn(TOPIC_ARN)
                .withSubject("Floating average warning")
                .withMessage(expectedMessage);
        verify(snsClient).publish(expectedPublishRequest);
    }

    @Test
    public void should_recommend_sell_if_stock_value_is_lower_than_average() throws Exception {
        BigDecimal stockValue = new BigDecimal("1134.23");
        BigDecimal floatingAverage = new BigDecimal("1234.55");
        CalculationData calculationData = ImmutableCalculationData.builder()
                .closingQuote(stockValue)
                .floatingAverage(floatingAverage)
                .percentageThreshold(new BigDecimal("5"))
                .stockSymbol(StockSymbol.DAX)
                .alertState(StockState.AlertState.SELL)
                .build();

        snsAlert.alert(calculationData);

        String expectedMessage = "Recommendation: sell!\nThe current closing value 1134.23 of observed stock DAX " +
                "deviates more than 5 percent from the current floating average of 1234.55.";
        PublishRequest expectedPublishRequest = new PublishRequest()
                .withTopicArn(TOPIC_ARN)
                .withSubject("Floating average warning")
                .withMessage(expectedMessage);
        verify(snsClient).publish(expectedPublishRequest);
    }
}
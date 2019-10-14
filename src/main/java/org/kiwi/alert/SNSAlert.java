package org.kiwi.alert;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

import org.kiwi.model.CalculationData;

public class SNSAlert implements DeviationAlert {

    private final AmazonSNSClient snsClient;
    private final String topic;

    @Inject
    SNSAlert(AmazonSNSClient snsClient, @Named("sns.alert.topic.arn") String topic) {
        this.snsClient = snsClient;
        this.topic = topic;
    }

    @Override
    public void alert(CalculationData calculationData) {
        PublishRequest publishRequest = createPublishRequestWith(calculationData);
        snsClient.publish(publishRequest);
    }

    private PublishRequest createPublishRequestWith(CalculationData calculationData) {
        String subject = "Floating average warning";
        String messageBody = createMessageBodyWith(calculationData);
        return new PublishRequest()
                .withTopicArn(topic)
                .withSubject(subject)
                .withMessage(messageBody);
    }

    private String createMessageBodyWith(CalculationData calculationData) {
        String recommendation = isStockValueHigherThanAverage(calculationData) ? "buy" : "sell";
        return "Recommendation: " + recommendation + "!\n" +
                "The current closing value " + calculationData.getClosingQuote().toPlainString() +
                " of observed stock " + calculationData.getStockSymbol().name() +
                " deviates more than " + calculationData.getPercentageThreshold() +
                " percent from the current floating average of " + calculationData.getFloatingAverage().toPlainString() + ".";
    }

    private boolean isStockValueHigherThanAverage(CalculationData calculationData) {
        return calculationData.getClosingQuote().compareTo(calculationData.getFloatingAverage()) == 1;
    }
}

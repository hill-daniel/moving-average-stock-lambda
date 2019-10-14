package org.kiwi;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.kiwi.alert.DeviationAlert;
import org.kiwi.alert.SNSAlert;

import java.io.InputStream;
import java.time.Clock;
import java.util.Properties;

import static com.amazonaws.regions.Regions.EU_CENTRAL_1;
import static com.google.inject.name.Names.bindProperties;

public class FloatingAverageLambdaModule extends AbstractModule {

    private static final String CONFIG_PROPERTIES = "/config.properties";

    @Override
    protected void configure() {
        bind(DeviationAlert.class).to(SNSAlert.class);
        bindProperties(binder(), loadProperties());
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new Jdk8Module());
    }

    @Provides
    @Singleton
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Provides
    @Singleton
    public AmazonSNSClient amazonSNSClient() {
        return new AmazonSNSAsyncClient()
                .<AmazonSNSAsyncClient>withRegion(Region.getRegion(EU_CENTRAL_1));
    }

    @Provides
    @Singleton
    public AmazonDynamoDBClient amazonDynamoDBClient(@Named("aws.dynamodb.endpoint") String dynamoDbEndpoint) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(EU_CENTRAL_1));
        client.setEndpoint(dynamoDbEndpoint);
        return client;
    }

    @Provides
    @Singleton
    public DynamoDB dynamoDB(AmazonDynamoDBClient amazonDynamoDBClient) {
        return new DynamoDB(amazonDynamoDBClient);
    }

    private Properties loadProperties() {
        try {
            Properties properties = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream(CONFIG_PROPERTIES)) {
                properties.load(inputStream);
                return properties;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties from [/config.properties]", e);
        }
    }
}

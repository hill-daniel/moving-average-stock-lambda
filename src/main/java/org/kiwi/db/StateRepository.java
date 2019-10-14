package org.kiwi.db;

import com.google.inject.Inject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.kiwi.quote.StockSymbol;

import java.io.IOException;

import static org.kiwi.db.FloatingAverageDynamoDbTable.HASH_KEY_NAME;
import static org.kiwi.db.FloatingAverageDynamoDbTable.RANGE_KEY_NAME;
import static org.kiwi.db.FloatingAverageDynamoDbTable.STOCK_STATE;
import static org.kiwi.db.FloatingAverageDynamoDbTable.TABLE_NAME;
import static org.kiwi.db.StockState.NEW_STATE;

public class StateRepository {

    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    @Inject
    public StateRepository(ItemRepository itemRepository, ObjectMapper objectMapper) {
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    public void persist(StockState stockState, StockSymbol stockSymbol) {
        Item stateItem = createStateItem(stockState, stockSymbol.name());
        itemRepository.put(TABLE_NAME, stateItem);
    }

    public StockState retrieveStateFor(StockSymbol stockSymbol) {
        HashRangeCompositeKey compositeKey = createCompositeKeyFor(stockSymbol);
        Item item = itemRepository.get(TABLE_NAME, compositeKey);
        return item != null ? parseFrom(item) : NEW_STATE;
    }

    private HashRangeCompositeKey createCompositeKeyFor(StockSymbol stockSymbol) {
        return org.kiwi.db.ImmutableHashRangeCompositeKey.builder()
                    .hashKeyName(HASH_KEY_NAME)
                    .hashKeyValue(STOCK_STATE + ":" + stockSymbol.name())
                    .rangeKeyName(RANGE_KEY_NAME)
                    .rangeKeyValue(0).build();
    }

    private Item createStateItem(StockState stockState, String stockId) {
        return new Item()
                .withPrimaryKey(new PrimaryKey(HASH_KEY_NAME, STOCK_STATE + ":" + stockId))
                .withInt(RANGE_KEY_NAME, 0)
                .withString(STOCK_STATE, toJson(stockState));
    }

    private String toJson(StockState stockState) {
        try {
            return objectMapper.writeValueAsString(stockState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create JSON from [" + stockState + "]");
        }
    }

    private StockState parseFrom(Item item) {
        String stateAsJson = item.getString(STOCK_STATE);
        try {
            return objectMapper.readValue(stateAsJson, StockState.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse StockState from JSON [" + stateAsJson + "]");
        }
    }
}

package org.kiwi.db;


import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.db.ImmutableHashRangeCompositeKey.builder;
import static org.kiwi.quote.StockSymbol.DAX;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StateRepositoryTest {

    private ItemRepository itemRepository;
    private StateRepository stateRepository;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        itemRepository = mock(ItemRepository.class);
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        stateRepository = new StateRepository(itemRepository, objectMapper);
    }

    @Test
    public void should_return_new_state_if_stock_has_no_state_yet() throws Exception {
        StockState stockState = stateRepository.retrieveStateFor(DAX);

        assertThat(stockState).isEqualTo(StockState.NEW_STATE);
    }

    @Test
    public void should_retrieve_state() throws Exception {
        String currentStateAsJson = "{\"currentTableIndex\":0,\"alertState\":\"NONE\"}";
        Item stateItem = new Item()
                .withPrimaryKey(new PrimaryKey("floatId", "currentState:DAX"))
                .withInt("rangeIndex", 0)
                .withString("currentState", currentStateAsJson);
        HashRangeCompositeKey compositeKey = builder()
                .hashKeyName("floatId")
                .hashKeyValue("currentState:DAX")
                .rangeKeyName("rangeIndex")
                .rangeKeyValue(0).build();
        when(itemRepository.get("floating_average", compositeKey))
                .thenReturn(stateItem);

        StockState stockState = stateRepository.retrieveStateFor(DAX);

        StockState expectedState = objectMapper.readValue(currentStateAsJson, StockState.class);
        assertThat(stockState).isEqualTo(expectedState);
    }

    @Test
    public void should_persist_state() throws Exception {
        String currentStateAsJson = "{\"currentTableIndex\":1,\"alertState\":\"BUY\"}";
        StockState currentState = objectMapper.readValue(currentStateAsJson, StockState.class);

        stateRepository.persist(currentState, DAX);

        Item expectedItem = new Item()
                .withPrimaryKey(new PrimaryKey("floatId", "currentState:DAX"))
                .withInt("rangeIndex", 0)
                .withString("currentState", currentStateAsJson);
        verify(itemRepository).put("floating_average", expectedItem);
    }
}
package org.kiwi.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableStockState.class)
@JsonDeserialize(as = ImmutableStockState.class)
public interface StockState {

    @JsonIgnore
    StockState NEW_STATE = ImmutableStockState.builder()
            .currentTableIndex(0)
            .alertState(AlertState.NONE)
            .build();

    enum AlertState {
        BUY, SELL, NONE
    }

    int getCurrentTableIndex();

    AlertState getAlertState();
}

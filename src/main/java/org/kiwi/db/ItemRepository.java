package org.kiwi.db;

import com.google.inject.Inject;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

class ItemRepository {

    private final DynamoDB dynamoDB;

    @Inject
    ItemRepository(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    Item get(String tableName, HashRangeCompositeKey compositeKey) {
        Table table = dynamoDB.getTable(tableName);
        return table.getItem(compositeKey.getHashKeyName(), compositeKey.getHashKeyValue(), compositeKey.getRangeKeyName(), compositeKey.getRangeKeyValue());
    }

    Item get(String tableName, PrimaryKey primaryKey) {
        Table table = dynamoDB.getTable(tableName);
        return table.getItem(primaryKey);
    }

    void put(String tableName, Item item) {
        Table table = dynamoDB.getTable(tableName);
        table.putItem(item);
    }

    Collection<Item> getWithQuery(String tableName, QuerySpec querySpec) {
        Table table = dynamoDB.getTable(tableName);
        ItemCollection<QueryOutcome> itemCollection = table.query(querySpec);
        return stream(itemCollection.spliterator(), false)
                .collect(toList());
    }
}

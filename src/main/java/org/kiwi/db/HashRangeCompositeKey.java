package org.kiwi.db;

import org.immutables.value.Value;

@Value.Immutable
interface HashRangeCompositeKey {

    String getHashKeyName();

    Object getHashKeyValue();

    String getRangeKeyName();

    Object getRangeKeyValue();
}

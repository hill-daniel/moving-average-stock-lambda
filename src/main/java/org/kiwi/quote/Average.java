package org.kiwi.quote;

import java.math.BigDecimal;
import java.util.Collection;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;

public class Average {

    public BigDecimal calculateFor(Collection<BigDecimal> quotes) {
        BigDecimal stockIndexQuoteSum = sum(quotes);
        return stockIndexQuoteSum.divide(new BigDecimal(quotes.size()), CEILING);
    }

    private BigDecimal sum(Collection<BigDecimal> quotes) {
        return quotes.stream()
                .reduce(ZERO, BigDecimal::add);
    }
}

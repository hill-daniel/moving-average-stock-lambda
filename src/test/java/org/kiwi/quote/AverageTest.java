package org.kiwi.quote;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.HistoricalQuotes;

import java.math.BigDecimal;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class AverageTest {

    private Average average;
    private HistoricalQuotes historicalQuotes;

    @Before
    public void setUp() throws Exception {
        average = new Average();
        historicalQuotes = new HistoricalQuotes();
    }

    @Test
    public void should_calculate_average_from_list_of_quotes() throws Exception {
        Collection<BigDecimal> closingQuotes = historicalQuotes.getClosingQuotes();

        BigDecimal average = this.average.calculateFor(closingQuotes);

        assertThat(average).isEqualTo(new BigDecimal("10019.997793"));
    }
}
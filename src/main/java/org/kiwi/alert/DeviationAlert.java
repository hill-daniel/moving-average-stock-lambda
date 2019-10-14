package org.kiwi.alert;

import org.kiwi.model.CalculationData;

@FunctionalInterface
public interface DeviationAlert {

    void alert(CalculationData calculationData);
}

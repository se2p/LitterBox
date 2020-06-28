package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public interface MetricAnalyzer<T extends Number> {

    T calculateMetric(Program program);

    String getName();
}

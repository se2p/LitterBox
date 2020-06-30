package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public interface MetricExtractor {

    double calculateMetric(Program program);

    String getName();
}

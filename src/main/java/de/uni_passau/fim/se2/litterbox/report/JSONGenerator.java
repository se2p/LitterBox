package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.analytics.MetricTool;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public abstract class JSONGenerator {
    void addMetrics(ObjectNode metricsNode, Program program) {
        MetricTool tool = new MetricTool();

        for (MetricExtractor metric : tool.getAnalyzers()) {
            double value = metric.calculateMetric(program);
            metricsNode.put(metric.getName(), value);
        }
    }
}

/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.core.JsonGenerator;
import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.analytics.MetricTool;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.IOException;

public abstract class JSONGenerator {
    void addMetrics(JsonGenerator jsonGenerator, Program program) throws IOException {
        MetricTool tool = new MetricTool();

        for (MetricExtractor metric : tool.getAnalyzers()) {
            double value = metric.calculateMetric(program);
            jsonGenerator.writeNumberField(metric.getName(), value);
        }
    }
}

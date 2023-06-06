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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.mblock.metric.*;
import de.uni_passau.fim.se2.litterbox.analytics.metric.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetricTool {
    private static final boolean LOAD_GENERAL = PropertyLoader.getSystemBooleanProperty("issues.load_general");
    private static final boolean LOAD_MBLOCK = PropertyLoader.getSystemBooleanProperty("issues.load_mblock");

    private List<MetricExtractor<Program>> getMetrics() {
        List<MetricExtractor<Program>> metricList = new ArrayList<>();
        if (LOAD_GENERAL) {
            metricList.add(new AvgBlockStatementCount<>());
            metricList.add(new AvgScriptWidthCount<>());
            metricList.add(new AvgVariableLengthCount<>());
            metricList.add(new BackdropCount<>());
            metricList.add(new BlockCount<>());
            metricList.add(new BooleanOperationCount<>());
            metricList.add(new CategoryEntropy<>());
            metricList.add(new Cohesion<>());
            metricList.add(new ComputationalThinkingAverageScore());
            metricList.add(new ComputationalThinkingScore());
            metricList.add(new ComputationalThinkingScoreAbstraction());
            metricList.add(new ComputationalThinkingScoreDataRepresentation());
            metricList.add(new ComputationalThinkingScoreFlowControl());
            metricList.add(new ComputationalThinkingScoreLogic());
            metricList.add(new ComputationalThinkingScoreParallelization());
            metricList.add(new ComputationalThinkingScoreSynchronization());
            metricList.add(new ComputationalThinkingScoreUserInteractivity());
            metricList.add(new ControlBlockCount<>());
            metricList.add(new CostumeCount<>());
            metricList.add(new EventsBlockCount<>());
            metricList.add(new HalsteadDifficulty<>());
            metricList.add(new HalsteadEffort<>());
            metricList.add(new HalsteadLength<>());
            metricList.add(new HalsteadVocabulary<>());
            metricList.add(new HalsteadVolume<>());
            metricList.add(new HatCount<>());
            metricList.add(new InterproceduralCyclomaticComplexity<>());
            metricList.add(new InterproceduralSliceCoverage<>());
            metricList.add(new LengthLongestScript<>());
            metricList.add(new ListUseCount<>());
            metricList.add(new LooksBlockCount<>());
            metricList.add(new MostComplexScript<>());
            metricList.add(new MotionBlockCount<>());
            metricList.add(new MusicBlockCount<>());
            metricList.add(new MyBlocksBlockCount<>());
            metricList.add(new NestedBlockCount<>());
            metricList.add(new OperatorsBlockCount<>());
            metricList.add(new PenBlockCount<>());
            metricList.add(new ProcedureCount<>());
            metricList.add(new ProgramUsingPen<>());
            metricList.add(new ScriptCount<>());
            metricList.add(new SensingBlockCount<>());
            metricList.add(new SoundBlockCount<>());
            metricList.add(new SpriteCount<>());
            metricList.add(new StackedStatementCount<>());
            metricList.add(new StatementCount<>());
            metricList.add(new TokenEntropy<>());
            metricList.add(new TranslateBlockCount<>());
            metricList.add(new UndefinedBlockCount<>());
            metricList.add(new VariableCount<>());
            metricList.add(new VariablesBlockCount<>());
            metricList.add(new VariableUseCount<>());
            metricList.add(new WeightedMethodCount<>());
            metricList.add(new WeightedMethodCountStrict<>());
        }
        if (LOAD_MBLOCK) {
            metricList.add(new AmbientLightBlockCount<>());
            metricList.add(new AurigaCounterMetric());
            metricList.add(new BatteryBlockCount<>());
            metricList.add(new CodeyCounterMetric());
            metricList.add(new ColorDetectionBlockCount<>());
            metricList.add(new DistanceBlockCount<>());
            metricList.add(new GearPotentiometerBlockCount<>());
            metricList.add(new GyroBlockCount<>());
            metricList.add(new LineFollowingBlockCount<>());
            metricList.add(new LoudnessBlockCount<>());
            metricList.add(new MBlockCount<>());
            metricList.add(new MCoreCounterMetric());
            metricList.add(new MegapiCounterMetric());
            metricList.add(new RealCodeyMetric());
            metricList.add(new RealMCoreMetric());
            metricList.add(new RealRobotMetric());
            metricList.add(new RobotCodeMetric());
            metricList.add(new RockyLightUsed<>());
            metricList.add(new ShakingStrengthBlockCount<>());
        }
        return metricList;
    }

    public List<String> getMetricNames() {
        return getMetrics().stream().map(MetricExtractor::getName).collect(Collectors.toList());
    }

    public List<MetricExtractor<Program>> getAnalyzers() {
        return Collections.unmodifiableList(getMetrics());
    }

    public void createCSVFile(Program program, Path fileName) throws IOException {
        final List<String> headers = new ArrayList<>();
        headers.add("project");
        getMetrics().stream().map(MetricExtractor::getName).forEach(headers::add);

        final List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());

        for (MetricExtractor<Program> extractor : getMetrics()) {
            row.add(Double.toString(extractor.calculateMetric(program)));
        }

        try (CSVPrinter printer = CSVPrinterFactory.getNewPrinter(fileName, headers)) {
            printer.printRecord(row);
            printer.flush();
        }
    }
}

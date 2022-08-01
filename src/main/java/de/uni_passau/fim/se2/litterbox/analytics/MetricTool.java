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

import de.uni_passau.fim.se2.litterbox.analytics.metric.*;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.metric.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetricTool {
    private static final boolean LOAD_GENERAL = PropertyLoader.getSystemBooleanProperty("issues.load_general");
    private static final boolean LOAD_MBLOCK = PropertyLoader.getSystemBooleanProperty("issues.load_mblock");

    private List<MetricExtractor> getMetrics() {
        List<MetricExtractor> metricList = new ArrayList<>();
        if (LOAD_GENERAL) {
            metricList.add(new AvgBlockStatementCount<Program>());
            metricList.add(new AvgScriptWidthCount<>());
            metricList.add(new AvgVariableLengthCount<>());
            metricList.add(new BackdropCount<>());
            metricList.add(new BlockCount<Program>());
            metricList.add(new CategoryEntropy<Program>());
            metricList.add(new Cohesion<Program>());
            metricList.add(new ComputationalThinkingAverageScore());
            metricList.add(new ComputationalThinkingScore());
            metricList.add(new ComputationalThinkingScoreAbstraction());
            metricList.add(new ComputationalThinkingScoreDataRepresentation());
            metricList.add(new ComputationalThinkingScoreFlowControl());
            metricList.add(new ComputationalThinkingScoreLogic());
            metricList.add(new ComputationalThinkingScoreParallelization());
            metricList.add(new ComputationalThinkingScoreSynchronization());
            metricList.add(new ComputationalThinkingScoreUserInteractivity());
            metricList.add(new ControlBlockCount<Program>());
            metricList.add(new CostumeCount<>());
            metricList.add(new EventsBlockCount<Program>());
            metricList.add(new HalsteadDifficulty<Program>());
            metricList.add(new HalsteadEffort<Program>());
            metricList.add(new HalsteadLength<Program>());
            metricList.add(new HalsteadVocabulary<Program>());
            metricList.add(new HalsteadVolume<Program>());
            metricList.add(new HatCount<Program>());
            metricList.add(new InterproceduralCyclomaticComplexity<Program>());
            metricList.add(new InterproceduralSliceCoverage<Program>());
            metricList.add(new LengthLongestScript<Program>());
            metricList.add(new LooksBlockCount<Program>());
            metricList.add(new MostComplexScript<Program>());
            metricList.add(new MotionBlockCount<Program>());
            metricList.add(new MyBlocksBlockCount<Program>());
            metricList.add(new NestedBlockCount<Program>());
            metricList.add(new OperatorsBlockCount<Program>());
            metricList.add(new PenBlockCount<Program>());
            metricList.add(new ProcedureCount<Program>());
            metricList.add(new ProgramUsingPen<Program>());
            metricList.add(new ScriptCount<Program>());
            metricList.add(new SensingBlockCount<Program>());
            metricList.add(new SoundBlockCount<Program>());
            metricList.add(new SpriteCount<Program>());
            metricList.add(new StackedStatementCount<Program>());
            metricList.add(new StatementCount<Program>());
            metricList.add(new TokenEntropy<Program>());
            metricList.add(new VariableCount<Program>());
            metricList.add(new VariablesBlockCount<Program>());
            metricList.add(new WeightedMethodCount<Program>());
            metricList.add(new WeightedMethodCountStrict<Program>());
        }
        if (LOAD_MBLOCK) {  // MBlock
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

    public List<MetricExtractor> getAnalyzers() {
        return Collections.unmodifiableList(getMetrics());
    }

    public void createCSVFile(Program program, String fileName) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("project");
        getMetrics().stream().map(MetricExtractor::getName).forEach(headers::add);
        CSVPrinter printer = getNewPrinter(fileName, headers);
        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());

        for (MetricExtractor extractor : getMetrics()) {
            row.add(Double.toString(extractor.calculateMetric(program)));
        }
        printer.printRecord(row);
        printer.flush();
    }

    // TODO: Code clone -- same is in CSVReportGenerator
    protected CSVPrinter getNewPrinter(String name, List<String> heads) throws IOException {

        if (Files.exists(Paths.get(name))) {
            BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withSkipHeaderRecord());
        } else {
            BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(heads.toArray(new String[0])));
        }
    }
}

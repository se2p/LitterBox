/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.metric.AvgScriptWidthCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.*;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
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

public class FeatureTool {

    private List<MetricExtractor<ASTNode>> metrics = Arrays.asList(
            new AvgBlockStatementCount<>(),
            new AvgScriptWidthCount<>(),
            new AvgVariableLengthCount<>(),
            new BlockCount<>(),
            new ControlBlockCount<>(),
            new EventsBlockCount<>(),
            new HalsteadVolume<>(),
            new HalsteadLength<>(),
            new HalsteadVocabulary<>(),
            new HalsteadDifficulty<>(),
            new HalsteadEffort<>(),
            new LooksBlockCount<>(),
            new MaxBlockStatementCount<>(),
            new MaxScriptWidthCount<>(),
            new MaxVariableLengthCount<>(),
            new MotionBlockCount<>(),
            new MyBlocksBlockCount<>(),
            new NestedBlockCount<>(),
            new OperatorsBlockCount<>(),
            new SensingBlockCount<>(),
            new SoundBlockCount<>(),
            new StackedStatementCount<>(),
            new StatementCount<>(),
            new TokenEntropy<>(),
            new VariableCount<>(),
            new VariablesBlockCount<>(),
            new WeightedMethodCount<>()
    );

    public List<String> getMetricNames() {
        return metrics.stream().map(MetricExtractor::getName).collect(Collectors.toList());
    }

    public List<MetricExtractor<ASTNode>> getAnalyzers() {
        return Collections.unmodifiableList(metrics);
    }

    public void createCSVFile(Program program, String fileName) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("project");
        headers.add("id");
        metrics.stream().map(MetricExtractor::getName).forEach(headers::add);
        headers.add("scratch_block_code");
        CSVPrinter printer = getNewPrinter(fileName, headers);
        int actorCount = 0;
        List<ActorDefinition> actorDefinitions = getActors(program);
        for (ActorDefinition actorDefinition : actorDefinitions) {
            int scriptCount = 0;
            int procedureDefCount = 0;
            actorCount = actorCount + 1;
            List<ASTNode> targets = new ArrayList<>();
            targets.addAll(actorDefinition.getScripts().getScriptList());
            targets.addAll(actorDefinition.getProcedureDefinitionList().getList());

            for (ASTNode target : targets) {
                List<String> row = new ArrayList<>();
                row.add(program.getIdent().getName());
                String uniqueID = "";
                if(target instanceof Script){
                    scriptCount =scriptCount + 1;
                    uniqueID = "ACTOR"+actorCount+"_"+"SCRIPT"+scriptCount;
                }
                else if(target instanceof ProcedureDefinition){
                    procedureDefCount = procedureDefCount + 1;
                    uniqueID = "ACTOR"+actorCount+"_"+"PROCEDUREDEFINITION"+procedureDefCount;
                }

                row.add(uniqueID);

                for (MetricExtractor<ASTNode> extractor : metrics) {
                    row.add(Double.toString(extractor.calculateMetric(target)));
                }
                String stringScratchCode = getScratchBlockCode(target, program, actorDefinition);
                row.add(stringScratchCode);
                printer.printRecord(row);
            }
        }
        printer.flush();
    }

    private String getScratchBlockCode(ASTNode target, Program program,ActorDefinition actorDefinition) {
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        if(target instanceof ProcedureDefinition) {
            visitor.setProgram(program);
            visitor.setCurrentActor(actorDefinition);
        }
        visitor.begin();
        target.accept(visitor);
        visitor.end();
        return visitor.getScratchBlocks();
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

    private List<ActorDefinition> getActors(Program program) {
        ActorDefinitionList actorDefinitionList = program.getActorDefinitionList();
        return actorDefinitionList.getDefinitions();
    }
}

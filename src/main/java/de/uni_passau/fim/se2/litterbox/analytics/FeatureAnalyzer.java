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

import de.uni_passau.fim.se2.litterbox.analytics.metric.FeatureResult;
import de.uni_passau.fim.se2.litterbox.analytics.metric.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FeatureAnalyzer extends FileAnalyzer<List<FeatureResult>> {

    private static final Logger log = Logger.getLogger(FeatureAnalyzer.class.getName());

    private final ProgramFeatureAnalyzer analyzer;

    public FeatureAnalyzer(Path output, boolean delete) {
        super(new ProgramFeatureAnalyzer(), output, delete);
        this.analyzer = (ProgramFeatureAnalyzer) super.analyzer;
    }

    @Override
    protected void checkAndWrite(File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            return;
        }

        createCSVFile(program, output);
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, List<FeatureResult> checkResult)
            throws IOException {
        try {
            createCSVFile(program, output);
        } catch (IOException e) {
            log.warning("Could not create CSV File: " + output);
            throw e;
        }
    }

    private void createCSVFile(Program program, Path fileName) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("project");
        headers.add("id");
        analyzer.getMetrics().stream().map(MetricExtractor::getName).forEach(headers::add);
        headers.add("scratch_block_code");
        CSVPrinter printer = CSVPrinterFactory.getNewPrinter(fileName, headers);
        int actorCount = 0;
        List<ActorDefinition> actorDefinitions = AstNodeUtil.getActors(program, true).toList();
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
                String uniqueId = "";
                if (target instanceof Script) {
                    scriptCount = scriptCount + 1;
                    uniqueId = "ACTOR" + actorCount + "_" + "SCRIPT" + scriptCount;
                } else if (target instanceof ProcedureDefinition) {
                    procedureDefCount = procedureDefCount + 1;
                    uniqueId = "ACTOR" + actorCount + "_" + "PROCEDUREDEFINITION" + procedureDefCount;
                }

                row.add(uniqueId);

                for (MetricExtractor<ASTNode> extractor : analyzer.getMetrics()) {
                    row.add(Double.toString(extractor.calculateMetric(target)));
                }
                String stringScratchCode = getScratchBlockCode(target, program, actorDefinition);
                row.add(stringScratchCode);
                printer.printRecord(row);
            }
        }
        printer.flush();
        printer.close();
    }

    private String getScratchBlockCode(ASTNode target, Program program, ActorDefinition actorDefinition) {
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        if (target instanceof ProcedureDefinition) {
            visitor.setProgram(program);
            visitor.setCurrentActor(actorDefinition);
        }
        visitor.begin();
        target.accept(visitor);
        visitor.end();
        return visitor.getScratchBlocks();
    }
}

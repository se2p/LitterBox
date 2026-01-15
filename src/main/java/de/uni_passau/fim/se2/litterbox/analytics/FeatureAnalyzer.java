/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.metrics.FeatureResult;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FeatureAnalyzer extends FileAnalyzer<List<FeatureResult>> {

    private static final Logger log = Logger.getLogger(FeatureAnalyzer.class.getName());

    private final String actor;
    private final boolean perActor;

    private final ProgramFeatureAnalyzer analyzer;
    private CSVPrinter printer;

    public FeatureAnalyzer(Path output, boolean delete, String actor, boolean perActor, boolean collectAverage) {
        super(new ProgramFeatureAnalyzer(collectAverage), output, delete);
        this.analyzer = (ProgramFeatureAnalyzer) super.analyzer;
        this.actor = actor;
        this.perActor = perActor;
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

    @Override
    protected void beginAnalysis() throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("project");
        headers.add("id");
        analyzer.getMetrics().stream().map(MetricExtractor::getName).forEach(headers::add);
        if (output == null) {
            // Use default format but manually print headers to avoid double printing issues and control behavior
            printer = new CSVPrinter(System.out, CSVFormat.DEFAULT);
            printer.printRecord(headers);
        } else {
            printer = CSVPrinterFactory.getNewPrinter(output, headers);
        }
    }

    @Override
    protected void endAnalysis() throws IOException {
        if (printer != null) {
            printer.flush();
            if (output != null) {
                printer.close();
            }
        }
    }

    private void createCSVFile(Program program, Path fileName) throws IOException {
        int actorCount = 0;
        List<ActorDefinition> actorDefinitions = AstNodeUtil.getActors(program, true).toList();
        for (ActorDefinition actorDefinition : actorDefinitions) {
            String actorName = actorDefinition.getIdent().getName();
            if (actor != null && !actor.isBlank() && !actor.equals(actorName)) {
                continue;
            }

            int scriptCount = 0;
            int procedureDefCount = 0;
            actorCount = actorCount + 1;
            List<ASTNode> targets = new ArrayList<>();
            List<Script> scripts = actorDefinition.getScripts().getScriptList();
            List<ProcedureDefinition> procedures = actorDefinition.getProcedureDefinitionList().getList();

            if (perActor) {
                if ((scripts.size() + procedures.size()) > 0) {
                    targets.add(actorDefinition);
                }
            } else {
                targets.addAll(scripts);
                targets.addAll(procedures);
            }

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
                } else if (target instanceof ActorDefinition) {
                    uniqueId = actorName;
                }

                row.add(uniqueId);

                for (MetricExtractor<ASTNode> extractor : analyzer.getMetrics()) {
                    row.add(Double.toString(extractor.calculateMetric(target)));
                }
                if (fileName == null) {
                    printer.printRecord(row);
                } else {
                    printer.printRecord(row);
                }
            }
        }
        printer.flush();
    }
}

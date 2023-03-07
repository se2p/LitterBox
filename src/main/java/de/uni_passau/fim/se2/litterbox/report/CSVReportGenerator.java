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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtils;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CSVReportGenerator implements ReportGenerator {

    private final List<String> detectors;
    private final CSVPrinter printer;
    private final boolean outputPerScript;

    /**
     * CSVReportGenerator writes the results of an analyses for a given list of detectors to a file.
     *
     * @param fileName        of the file to which the report is written.
     * @param detectors       list of detectors that should be included in the report.
     * @param outputPerScript indicate if the results should be written per scripts
     * @throws IOException is thrown if the file cannot be opened
     */
    public CSVReportGenerator(String fileName, List<String> detectors, boolean outputPerScript) throws IOException {
        this.detectors = new ArrayList<>(detectors);
        this.outputPerScript = outputPerScript;

        final List<String> headers = new ArrayList<>();
        if (outputPerScript)
            headers.add("script");
        else
            headers.add("project");
        headers.addAll(this.detectors);

        printer = CSVPrinterFactory.getNewPrinter(fileName, headers);
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        List<String> row;
        if (outputPerScript) {
            for (ActorDefinition actorDefinition : program.getActorDefinitionList().getDefinitions()) {
                for (Script script : actorDefinition.getScripts().getScriptList()) {
                    row = generateReportsPerScript(program, issues, script);
                    printer.printRecord(row);
                }
                for (ProcedureDefinition procedureDefinition : actorDefinition.getProcedureDefinitionList().getList()) {
                    row = generateReportsPerScript(program, issues, procedureDefinition);
                    printer.printRecord(row);
                }
            }
        } else {
            row = generateReportsPerProject(program, issues);
            printer.printRecord(row);
        }
        printer.flush();
    }

    public void close() throws IOException {
        printer.close();
    }

    private List<String> generateReportsPerProject(Program program, Collection<Issue> issues) {
        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());
        for (String finder : detectors) {
            long numIssuesForFinder = issues
                    .stream()
                    .filter(i -> i.getFinderName().equals(finder))
                    .count();
            row.add(Long.toString(numIssuesForFinder));
        }
        return row;
    }

    private List<String> generateReportsPerScript(Program program, Collection<Issue> issues,  ScriptEntity scriptEntity) {
        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName() + "_" + NodeNameUtils.getSpriteOrProcedureDefinitionName(scriptEntity));
        for (String finder : detectors) {
            long numIssuesForFinder = issues
                    .stream()
                    .filter(i -> i.getScriptOrProcedureDefinition().equals(scriptEntity))
                    .filter(i -> i.getFinderName().equals(finder))
                    .count();
            row.add(Long.toString(numIssuesForFinder));
        }
        return row;
    }



}

/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.analytics.FileAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Analyzer that runs BugInjectors on Scratch programs and saves the mutated versions.
 */
public class InjectorAnalyzer extends FileAnalyzer<Map<BugInjector, List<InjectionResult>>> {

    private static final Logger log = Logger.getLogger(InjectorAnalyzer.class.getName());

    private static final List<String> CSV_HEADERS = List.of(
            "original_project",
            "bug_finder",
            "mutated_project",
            "sprite_name",
            "original_bug_count",
            "mutated_bug_count",
            "original_script",
            "mutated_script"
    );

    private final Path csvPath;
    private CSVPrinter csvPrinter;

    public InjectorAnalyzer(Path output, boolean delete, List<BugInjector> injectors) {
        this(output, delete, injectors, null);
    }

    public InjectorAnalyzer(Path output, boolean delete, List<BugInjector> injectors, Path csvPath) {
        super(new InjectorProgramAnalyzer(injectors), output, delete);
        this.csvPath = csvPath;
    }

    @Override
    protected void beginAnalysis() throws IOException {
        super.beginAnalysis();
        if (csvPath != null) {
            csvPrinter = CSVPrinterFactory.getNewPrinter(csvPath, CSV_HEADERS);
        }
    }

    @Override
    protected void endAnalysis() throws IOException {
        if (csvPrinter != null) {
            csvPrinter.close();
        }
        super.endAnalysis();
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Map<BugInjector, List<InjectionResult>> result) throws IOException {
        String originalExt = FilenameUtils.getExtension(projectFile.toString());
        String originalProjectName = program.getIdent().getName();

        int totalInjected = 0;
        for (Map.Entry<BugInjector, List<InjectionResult>> entry : result.entrySet()) {
            BugInjector injector = entry.getKey();
            List<InjectionResult> injectionResults = entry.getValue();

            int index = 0;
            for (InjectionResult injectionResult : injectionResults) {
                Program mutated = injectionResult.mutatedProgram();
                // Suffix: _BugName_Index
                String suffix = "_" + injector.getName() + "_" + index;
                String mutatedProjectName = originalProjectName + suffix;

                if (originalExt.equalsIgnoreCase("json")) {
                    JSONFileCreator.writeJsonFromProgram(mutated, output, suffix);
                } else {
                    JSONFileCreator.writeSb3FromProgram(mutated, output, projectFile.toFile(), suffix);
                }

                // Write to CSV if enabled
                if (csvPrinter != null) {
                    writeCSVRow(program, mutated, injector, mutatedProjectName, injectionResult);
                }

                index++;
                totalInjected++;
            }
        }
        log.info("Injected " + totalInjected + " bugs into " + projectFile.getFileName());
    }

    private void writeCSVRow(
            Program original,
            Program mutated,
            BugInjector injector,
            String mutatedProjectName,
            InjectionResult injectionResult
    ) throws IOException {
        IssueFinder finder = injector.getIssueFinder();
        String originalProjectName = original.getIdent().getName();

        // Count bugs
        Set<Issue> originalIssues = finder.check(original);
        Set<Issue> mutatedIssues = finder.check(mutated);

        csvPrinter.printRecord(
                originalProjectName,
                finder.getName(),
                mutatedProjectName,
                injectionResult.spriteName(),
                originalIssues.size(),
                mutatedIssues.size(),
                injectionResult.originalScriptScratchBlocks(),
                injectionResult.mutatedScriptScratchBlocks()
        );
        csvPrinter.flush();
    }

    private record InjectorProgramAnalyzer(
            List<BugInjector> injectors) implements ProgramAnalyzer<Map<BugInjector, List<InjectionResult>>> {

        @Override
        public Map<BugInjector, List<InjectionResult>> analyze(Program program) {
            Map<BugInjector, List<InjectionResult>> result = new LinkedHashMap<>();
            for (BugInjector injector : injectors) {
                try {
                    // Injector is responsible for cloning the program as needed.
                    List<InjectionResult> injected = injector.inject(program);
                    if (!injected.isEmpty()) {
                        result.put(injector, injected);
                    }
                } catch (Exception e) {
                    log.severe("Injector " + injector.getName() + " failed on project " + program.getIdent().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return result;
        }
    }
}

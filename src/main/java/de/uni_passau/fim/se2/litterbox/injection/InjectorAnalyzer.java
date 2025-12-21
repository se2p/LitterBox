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
import de.uni_passau.fim.se2.litterbox.analytics.ProgramAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Analyzer that runs BugInjectors on Scratch programs and saves the mutated versions.
 */
public class InjectorAnalyzer extends FileAnalyzer<Map<BugInjector, List<Program>>> {

    private static final Logger log = Logger.getLogger(InjectorAnalyzer.class.getName());

    public InjectorAnalyzer(Path output, boolean delete, List<BugInjector> injectors) {
        super(new InjectorProgramAnalyzer(injectors), output, delete);
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Map<BugInjector, List<Program>> result) throws IOException {
        String originalExt = FilenameUtils.getExtension(projectFile.toString());

        int totalInjected = 0;
        for (Map.Entry<BugInjector, List<Program>> entry : result.entrySet()) {
            BugInjector injector = entry.getKey();
            List<Program> mutatedPrograms = entry.getValue();

            int index = 0;
            for (Program mutated : mutatedPrograms) {
                // Suffix: _BugName_Index
                String suffix = "_" + injector.getName() + "_" + index;

                if (originalExt.equalsIgnoreCase("json")) {
                    JSONFileCreator.writeJsonFromProgram(mutated, output, suffix);
                } else {
                    JSONFileCreator.writeSb3FromProgram(mutated, output, projectFile.toFile(), suffix);
                }
                index++;
                totalInjected++;
            }
        }
        log.info("Injected " + totalInjected + " bugs into " + projectFile.getFileName());
    }

    private record InjectorProgramAnalyzer(
            List<BugInjector> injectors) implements ProgramAnalyzer<Map<BugInjector, List<Program>>> {

        @Override
            public Map<BugInjector, List<Program>> analyze(Program program) {
                Map<BugInjector, List<Program>> result = new LinkedHashMap<>();
                for (BugInjector injector : injectors) {
                    try {
                        // Injector is responsible for cloning the program as needed.
                        List<Program> injected = injector.inject(program);
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

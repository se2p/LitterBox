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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.AnalysisResultHandler;
import de.uni_passau.fim.se2.litterbox.report.CommentGenerator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;

public class BugAnalyzer extends FileAnalyzer<Set<Issue>> {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());

    private static final String ANNOTATED_PROGRAM_SUFFIX = "_annotated";

    private final IssueTranslator translator;
    private final AnalysisResultHandler resultHandler;
    private Path annotationOutput;

    public BugAnalyzer(
            IssueTranslator translator, Path output, String detectors,
            boolean ignoreLooseBlocks, boolean delete, boolean outputPerScript
    ) {
        super(new ProgramBugAnalyzer(detectors, ignoreLooseBlocks), output, delete);
        this.translator = translator;
        this.resultHandler = AnalysisResultHandler.create(translator, output, detectors, outputPerScript);
    }

    public BugAnalyzer(
            IssueTranslator translator, Path output, String detectors,
            boolean ignoreLooseBlocks, boolean delete, boolean outputPerScript, Path priorResultPath
    ) {
        super(new ProgramBugAnalyzer(detectors, ignoreLooseBlocks), output, delete);
        this.translator = translator;
        this.resultHandler = AnalysisResultHandler.create(translator, output, detectors, outputPerScript);
    }

    public void setAnnotationOutput(Path annotationOutput) {
        this.annotationOutput = annotationOutput;
    }

    @Override
    protected void beginAnalysis() throws IOException {
        resultHandler.begin();
        super.beginAnalysis();
    }

    @Override
    protected void endAnalysis() throws IOException {
        resultHandler.finish();
        super.endAnalysis();
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Set<Issue> result) {
        resultHandler.handleResult(projectFile, program, result);
        createAnnotatedFile(projectFile.toFile(), program, result, annotationOutput);
    }

    private void createAnnotatedFile(File fileEntry, Program program, Set<Issue> issues, Path annotatePath) {
        if (annotationOutput == null) {
            return;
        }

        try {
            CommentGenerator commentGenerator = new CommentGenerator(translator);
            commentGenerator.generateReport(program, issues);
            String fileExtension = FilenameUtils.getExtension(fileEntry.getPath());

            if (fileExtension.equalsIgnoreCase("json")) {
                JSONFileCreator.writeJsonFromProgram(program, annotatePath, ANNOTATED_PROGRAM_SUFFIX);
            } else if (fileExtension.equalsIgnoreCase("sb3")) {
                JSONFileCreator.writeSb3FromProgram(program, annotatePath, fileEntry, ANNOTATED_PROGRAM_SUFFIX);
            } else {
                JSONFileCreator.writeMBlockFromProgram(program, annotatePath, fileEntry, ANNOTATED_PROGRAM_SUFFIX);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
}

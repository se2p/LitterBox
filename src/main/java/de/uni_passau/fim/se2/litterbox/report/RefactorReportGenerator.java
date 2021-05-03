package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.io.IOException;
import java.util.Collection;

public interface RefactorReportGenerator {

    void generateReport(Program program, Collection<Refactoring> refactorings) throws IOException;
}

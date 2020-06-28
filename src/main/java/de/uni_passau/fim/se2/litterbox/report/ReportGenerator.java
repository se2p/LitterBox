package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.IOException;
import java.util.Collection;

public interface ReportGenerator {

    void generateReport(Program program, Collection<Issue> issues) throws IOException;

}

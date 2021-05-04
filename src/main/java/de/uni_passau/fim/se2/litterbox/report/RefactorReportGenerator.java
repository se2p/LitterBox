package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.io.IOException;

public interface RefactorReportGenerator {

    void generateReport(Program program, RefactorSequence refactorSequence) throws IOException;
}

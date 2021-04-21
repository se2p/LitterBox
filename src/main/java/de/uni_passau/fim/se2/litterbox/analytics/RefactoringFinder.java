package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.List;

public interface RefactoringFinder {

    /**
     * Checks the given program for a specific refactoring.
     *
     * @param program The project to check
     * @return a set of instantiated possible refactorings
     */
    List<Refactoring> check(Program program);

    void setIgnoreLooseBlocks(boolean value);

    String getName();
}

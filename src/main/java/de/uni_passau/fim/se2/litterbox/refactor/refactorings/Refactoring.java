package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * Interface for all Refactorings.
 */
public interface Refactoring {

    /**
     * Apply the refactoring onto the given program and return the new modified program tree.
     *
     * @param program The program before the refactoring.
     * @return A copy of the program after the refactoring.
     */
    Program apply(Program program);

    String getName();
}

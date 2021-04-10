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

    /**
     * Checks if the refactoring can be applied.
     *
     * @return {@code true} if the refactoring can be applied, {@code false} otherwise
     */
    boolean preCondition();

    /**
     * Checks if the refactoring was correctly applied and leaves the program in a valid state.
     *
     * @return {@code true} if the program is still valid after the refactoring, {@code false} otherwise
     */
    boolean postCondition();

    String getName();
}

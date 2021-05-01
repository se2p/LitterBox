package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * Interface for all Refactorings.
 */
public interface Refactoring {

    /**
     * Apply the refactoring onto the given program and return the new modified program tree. Note to only apply the
     * refactoring after a deep copy of the original program was made with the {@code deepCopy()} method to avoid
     * transformations on the original program tree!
     *
     * @param program The program before the refactoring.
     * @return The refactored program.
     */
    Program apply(Program program);

    String getName();
}

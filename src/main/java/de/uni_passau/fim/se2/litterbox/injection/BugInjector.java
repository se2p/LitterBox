package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.List;

/**
 * Interface that all BugInjectors must implement.
 */
public interface BugInjector {

    /**
     * Returns a list of modified programs, each containing one instance of the injected bug.
     *
     * @param program The original program to inject bugs into.
     * @return List of mutated programs.
     */
    List<Program> inject(Program program);

    /**
     * @return The canonical name of the bug injector.
     */
    String getName();

    /**
     * @return The IssueFinder associated with this bug injector.
     */
    IssueFinder getIssueFinder();
}

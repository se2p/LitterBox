package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

/**
 * Represents a minimizing fitness function, which awards lower values to better solutions.
 *
 * @param <C> the type of configuration rated by this function
 */
@FunctionalInterface
public interface MinimizingFitnessFunction<C> extends FitnessFunction<C> {

    /**
     * Always returns {@code true} as this is a minimizing fitness function.
     *
     * @return always {@code true}
     */
    @Override
    default boolean isMinimizing() {
        return true;
    }

    /**
     * Always returns {@code 1} as this is a minimizing fitness function.
     *
     * @return always {@code 1}
     */
    @Override
    default double getReferencePoint() {
        return 1;
    }
}

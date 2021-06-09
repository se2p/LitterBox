package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

/**
 * Represents a maximizing fitness function, which awards higher values to better solutions.
 *
 * @param <C> the type of configuration rated by this function
 */
@FunctionalInterface
public interface MaximizingFitnessFunction<C> extends FitnessFunction<C> {

    /**
     * Always returns {@code false} as this is a maximizing fitness function.
     *
     * @return always {@code false}
     */
    @Override
    default boolean isMinimizing() {
        return false;
    }

    /**
     * Always returns {@code 0} as this is a maximizing fitness function.
     *
     * @return always {@code 0}
     */
    @Override
    default double getReferencePoint() {
        return 0;
    }
}

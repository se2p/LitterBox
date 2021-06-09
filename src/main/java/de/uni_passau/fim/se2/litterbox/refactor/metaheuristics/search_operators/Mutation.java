package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;

import java.util.function.UnaryOperator;

/**
 * Mutation introduces new genetic material in offspring by modifying a parent chromosome.
 *
 * @param <C> the type of chromosomes supported by this mutation operator
 * @apiNote Usually, it is desired that the offspring of two chromosomes of type {@code C} is again
 * of the same type {@code C}. This requirement can be enforced at compile time by specifying a
 * recursive type parameter, here: {@code C extends Chromosome<C>}.
 */
@FunctionalInterface
public interface Mutation<C extends Chromosome<C>> extends UnaryOperator<C> {

    /**
     * A mutation operator that always returns the parent chromosome as offspring.
     *
     * @param <C> the type of chromosome
     * @return a mutation operator that always returns the parent as offspring
     * @apiNote Can be useful for creating dummy chromosomes when writing unit tests.
     */
    static <C extends Chromosome<C>> Mutation<C> identity() {
        return C::copy;
    }

    /**
     * Applies mutation to the given chromosome {@code c} and returns the resulting offspring.
     * Usually, it is desirable that the parent chromosome not be modified in-place. Instead, it is
     * advisable to create a copy of the parent, mutate the copy and return it as offspring. While
     * this is not an absolute requirement implementations that do not conform to this should
     * clearly indicate this fact.
     *
     * @param c the parent chromosome to mutate
     * @return the offspring formed by mutating the parent
     */
    @Override
    C apply(final C c);
}

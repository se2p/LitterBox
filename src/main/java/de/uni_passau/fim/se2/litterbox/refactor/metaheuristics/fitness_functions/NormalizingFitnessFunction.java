package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

public class NormalizingFitnessFunction<C> implements FitnessFunction<C> {
    private FitnessFunction<C> wrappedFitnessFunction;

    public NormalizingFitnessFunction(FitnessFunction<C> wrappedFitnessFunction) {
        this.wrappedFitnessFunction = wrappedFitnessFunction;
    }

    @Override
    public double getFitness(C c) throws NullPointerException {
        double fitness = wrappedFitnessFunction.getFitness(c);
        return fitness / (1.0 + fitness);
    }

    @Override
    public boolean isMinimizing() {
        return wrappedFitnessFunction.isMinimizing();
    }

    @Override
    public double getReferencePoint() {
        return wrappedFitnessFunction.isMinimizing() ? 1 : 0;
    }
}

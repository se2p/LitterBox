/*
 * Copyright (C) 2019-2021 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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

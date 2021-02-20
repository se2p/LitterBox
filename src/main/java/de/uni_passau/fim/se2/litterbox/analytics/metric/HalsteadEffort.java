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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.HalsteadVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class HalsteadEffort implements MetricExtractor, ScratchVisitor {

    @Override
    public double calculateMetric(Program program) {
        HalsteadVisitor halstead = new HalsteadVisitor();
        program.accept(halstead);
        // E = V * D

        int length = halstead.getTotalOperands() + halstead.getTotalOperators();
        int size = halstead.getUniqueOperands() + halstead.getUniqueOperators();
        double volume = length * Math.log(size) / Math.log(2);

        //  D = ( n1 / 2 ) * ( N2 / n2 )
        double n1 = halstead.getUniqueOperators();
        double n2  = halstead.getUniqueOperands();
        double totalOperands = halstead.getTotalOperands();

        double difficulty = (n1 / 2.0) * (totalOperands / n2);

        return volume * difficulty;
    }

    @Override
    public String getName() {
        return "halstead_effort";
    }
}

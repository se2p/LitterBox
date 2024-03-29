/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.HalsteadVisitor;

public class HalsteadVolume<T extends ASTNode> implements MetricExtractor<T> {

    @Override
    public double calculateMetric(T node) {
        HalsteadVisitor halstead = new HalsteadVisitor();
        node.accept(halstead);
        // The program volume (V) is the information contents of the program, measured in mathematical bits
        // It is calculated as the program length times the 2-base logarithm of the vocabulary size (n):
        //        V = N * log2(n)
        int length = halstead.getTotalOperands() + halstead.getTotalOperators();
        int size = halstead.getUniqueOperands() + halstead.getUniqueOperators();
        if (size == 0) {
            // Empty project
            return 0;
        }
        return length * Math.log(size) / Math.log(2);
    }

    @Override
    public String getName() {
        return "halstead_volume";
    }
}

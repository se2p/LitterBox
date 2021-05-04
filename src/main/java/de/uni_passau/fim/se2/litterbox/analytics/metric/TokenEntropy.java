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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

public class TokenEntropy<T extends ASTNode> implements MetricExtractor<T> {

    @Override
    public double calculateMetric(T node) {
        TokenVisitor visitor = new TokenVisitor();
        node.accept(visitor);

        double entropyValue = 0.0;
        for(ASTNode token : visitor.getUniqueTokens()) {
            double p = ((double)visitor.getTokenCount(token)) / (double)visitor.getTotalTokenCount();
            double tokenEntropy = p * (Math.log(p)/Math.log(2.0));
            entropyValue += tokenEntropy;
        }
        return(-entropyValue);
    }

    @Override
    public String getName() {
        return "token_entropy";
    }
}

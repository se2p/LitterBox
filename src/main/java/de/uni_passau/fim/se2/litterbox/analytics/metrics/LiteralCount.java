/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.metrics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class LiteralCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "literal_count";

    /**
     * Option to calculate the proportion of literals in a program rather than their absolute count.
     * The name "average" comes from readability research (e.g. [1]) and was retained to comply with literature.
     *
     * <p>[1] Scalabrino et al.: A comprehensive model for code readability,
     * <a href="https://doi.org/10.1002/smr.1958">doi:10.1002/smr.1958</a>)
     */
    private final boolean average;
    private int literalCount = 0;

    public LiteralCount() {
        this.average = false;
    }

    public LiteralCount(boolean average) {
        this.average = average;
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        double totalCount = new BlockCount<T>().calculateMetric(node);
        if (totalCount == 0) {
            return 0.0;
        }
        literalCount = 0;
        node.accept(this);
        return average ? literalCount / totalCount : literalCount;
    }

    @Override
    public void visit(Program program) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            visit(actor.getScripts());
            visit(actor.getProcedureDefinitionList());
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        visit(actor.getScripts());
        visit(actor.getProcedureDefinitionList());
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        visit(procedure.getParameterDefinitionList());
        visit(procedure.getStmtList());
    }

    @Override
    public void visit(StringLiteral node) {
        if (!(node.getParentNode() instanceof StrId)) {
            literalCount++;
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        literalCount++;
    }

    @Override
    public String getName() {
        if (average) {
            return "average_" + NAME;
        }
        return NAME;
    }
}

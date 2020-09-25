/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.clonedetection;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class NormalizationVisitor extends CloneVisitor {

    @Override
    public ASTNode visit(StringLiteral node) {
        return new NormalizedStringLiteral();
    }

    @Override
    public ASTNode visit(NumberLiteral node) {
        return new NormalizedNumberLiteral();
    }

    @Override
    public ASTNode visit(Variable node) {
        return new NormalizedVariable();
    }

    @Override
    public ASTNode visit(ScratchList node) {
        return new NormalizedScratchList();
    }

    @Override
    public ASTNode visit(Parameter node) {
        return new NormalizedParameter();
    }

    @Override
    public ASTNode visit(ParameterDefinition node) {
        return new ParameterDefinition(new StrId(NormalizedParameter.NAME), node.getType(), node.getMetadata());
    }

    // TODO: Should the names of custom blocks and calls be normalised?
    // TODO: Parameters and Variables are not equal after normalization
}

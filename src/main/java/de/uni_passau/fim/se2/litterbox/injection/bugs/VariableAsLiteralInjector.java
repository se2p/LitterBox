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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.VariableAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.Optional;

/**
 * Injects the VariableAsLiteral bug by replacing Variable references with string literals of their names.
 */
public class VariableAsLiteralInjector extends AbstractAsLiteralInjector {

    private final IssueFinder issueFinder = new VariableAsLiteral();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    protected Optional<StringLiteral> extractIdentifierName(Expression expr) {
        // Check for direct Variable
        if (expr instanceof Variable variable) {
            return Optional.of(new StringLiteral(variable.getName().getName()));
        }
        // Check for Qualified Variable
        if (expr instanceof Qualified qualified && qualified.getSecond() instanceof Variable variable) {
            return Optional.of(new StringLiteral(variable.getName().getName()));
        }
        return Optional.empty();
    }

    @Override
    public void visit(Qualified qualified) {
        // Only attempt injection if this Qualified actually contains a Variable
        if (qualified.getSecond() instanceof Variable) {
            checkAndInject(qualified);
        }
        super.visit(qualified);
    }

    @Override
    public void visit(Variable variable) {
        checkAndInject(variable);
        super.visit(variable);
    }
}

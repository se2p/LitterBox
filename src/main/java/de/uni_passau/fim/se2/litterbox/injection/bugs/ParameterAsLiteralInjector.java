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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.Optional;

/**
 * Injects the ParameterAsLiteral bug by replacing Parameter references with string literals of their names.
 *
 * <p>
 * Unlike VariableAsLiteralInjector, this injector does NOT check for string context because
 * the ParameterAsLiteral finder detects StringLiterals matching parameter names regardless of
 * where they appear (in numeric, boolean, or string positions).
 */
public class ParameterAsLiteralInjector extends AbstractAsLiteralInjector {

    private final IssueFinder issueFinder = new ParameterAsLiteral();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    /**
     * Override to skip string context check - parameters can be replaced in any context
     * because the finder detects the bug based on literal text matching parameter names.
     */
    @Override
    protected boolean shouldCheckStringContext() {
        return false;
    }

    @Override
    protected Optional<StringLiteral> extractIdentifierName(Expression expr) {
        // Check for direct Parameter
        if (expr instanceof Parameter parameter) {
            return Optional.of(new StringLiteral(parameter.getName().getName()));
        }
        // Check for Qualified Parameter
        if (expr instanceof Qualified qualified && qualified.getSecond() instanceof Parameter parameter) {
            return Optional.of(new StringLiteral(parameter.getName().getName()));
        }
        return Optional.empty();
    }

    @Override
    public void visit(Qualified qualified) {
        // Only attempt injection if this Qualified actually contains a Parameter
        if (qualified.getSecond() instanceof Parameter) {
            checkAndInject(qualified);
        }
        super.visit(qualified);
    }

    @Override
    public void visit(Parameter parameter) {
        checkAndInject(parameter);
        super.visit(parameter);
    }
}

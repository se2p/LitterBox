/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow;

import de.uni_passau.fim.se2.litterbox.analytics.bugs.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Variable;
import de.uni_passau.fim.se2.litterbox.dataflow.TransferFunction;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class TypeTransferFunction implements TransferFunction<VariableTypeFact> {

    @Override
    public Set<VariableTypeFact> apply(CFGNode node, Set<VariableTypeFact> inFacts) {
        Set<VariableTypeFact> outFacts = new LinkedHashSet<>(inFacts);

        Set<Definition> definitions = node.getDefinitions();
        ASTNode stmt = node.getASTNode();

        for (Definition def : definitions) {
            Defineable definable = def.getDefinable();
            if (definable instanceof Variable variable) {
                // Kill old facts for this variable
                outFacts.removeIf(f -> f.getVariable().equals(variable));

                // Generate new fact
                Type type = Type.ANY; // Default fallback

                if (stmt instanceof SetVariableTo setStmt) {
                    if (setStmt.getIdentifier().equals(variable.getIdentifier())) {
                         type = TypeInference.inferType(setStmt.getExpr(), createResolver(inFacts));
                         if (type == null) {
                             type = Type.ANY;
                         }
                    }
                } else if (stmt instanceof ChangeVariableBy changeStmt) {
                    if (changeStmt.getIdentifier().equals(variable.getIdentifier())) {
                        type = Type.NUMBER;
                    }
                }

                outFacts.add(new VariableTypeFact(variable, type));
            }
        }

        return outFacts;
    }

    private Function<Identifier, Type> createResolver(Set<VariableTypeFact> facts) {
        return identifier -> {
            for (VariableTypeFact fact : facts) {
                if (fact.getVariable() instanceof Variable v) {
                    // Check logic equality of definition identifier
                    // We assume Variable equals checks Identifier equals.
                    if (v.getIdentifier().equals(identifier)) {
                        return fact.getType();
                    }
                }
            }
            return null;
        };
    }
}

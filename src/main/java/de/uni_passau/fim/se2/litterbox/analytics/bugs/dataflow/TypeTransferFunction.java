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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
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

        // Kill facts based on definitions
        if (stmt instanceof SetVariableTo setStmt) {
             Variable variable = new Variable(setStmt.getIdentifier());
             // Remove any previous facts for this variable (using loose equality) to avoid duplicates
             outFacts.removeIf(f -> f.getVariable() instanceof Variable v && idsEqual(v.getIdentifier(), setStmt.getIdentifier()));
             
             Type type = TypeInference.inferType(setStmt.getExpr(), createResolver(inFacts));
             if (type == null) type = Type.ANY;
             
             outFacts.add(new VariableTypeFact(variable, type));
        } else if (stmt instanceof ChangeVariableBy changeStmt) {
             Variable variable = new Variable(changeStmt.getIdentifier());
             outFacts.removeIf(f -> f.getVariable() instanceof Variable v && idsEqual(v.getIdentifier(), changeStmt.getIdentifier()));
             
             outFacts.add(new VariableTypeFact(variable, Type.NUMBER));
        } else {
             // For all other statements, check definitions
             for (Definition def : definitions) {
                 Defineable definable = def.getDefinable();
                 if (definable instanceof Variable variable) {
                      // We only care about Set/Change for specific processing.
                      // All other variable definitions (if any exist for other nodes) effectively clear knowledge or set to ANY?
                      // The initial kill loop already removed facts.
                      // If we are here, we killed the fact but didn't generate a new one.
                      // Should we generate ANY?
                      // If it's a variable definition that isn't set/change assignment, it's likely a parameter or loop variable?
                      // Loop variable assignment is implicit.
                      outFacts.add(new VariableTypeFact(variable, Type.ANY));
                 }
             }
        }

        return outFacts;
    }

    private Function<Identifier, Type> createResolver(Set<VariableTypeFact> facts) {
        return identifier -> {
            for (VariableTypeFact fact : facts) {
                if (fact.getVariable() instanceof Variable v) {
                    if (idsEqual(v.getIdentifier(), identifier)) {
                        return fact.getType();
                    }
                }
            }
            return null;
        };
    }

    private boolean idsEqual(Identifier a, Identifier b) {
        if (a instanceof LocalIdentifier la && b instanceof LocalIdentifier lb) {
            return la.getName().equals(lb.getName());
        }
        return a.equals(b);
    }
}

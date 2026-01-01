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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.uni_passau.fim.se2.litterbox.injection.util.InjectionConstants.MIN_PARAMS_REQUIRED;

/**
 * Injects ambiguous parameter naming by renaming the second used parameter
 * to match the first used parameter's name in a procedure definition.
 * All references in the body are updated to maintain syntactic validity
 * while introducing logical ambiguity.
 */
public class AmbiguousParameterNameUsedInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new AmbiguousParameterNameUsed();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        List<ParameterDefinition> params = node.getParameterDefinitionList().getParameterDefinitions();

        if (params.size() >= MIN_PARAMS_REQUIRED) {
            // Skip if ambiguous parameter names already exist
            if (hasAmbiguousParameterNames(params)) {
                super.visit(node);
                return;
            }

            List<ParameterDefinition> usedParams = findUsedParameters(node, params);

            if (usedParams.size() >= MIN_PARAMS_REQUIRED) {
                String nameToKeep = usedParams.get(0).getIdent().getName();
                String nameToReplace = usedParams.get(1).getIdent().getName();
                int indexToModify = params.indexOf(usedParams.get(1));

                modifyClonedNode(node, clonedDef -> applyParameterRename(
                        clonedDef, indexToModify, nameToReplace, nameToKeep));
            }
        }
        super.visit(node);
    }

    /**
     * Checks if any two parameters already have the same name.
     */
    private boolean hasAmbiguousParameterNames(List<ParameterDefinition> params) {
        Set<String> names = new HashSet<>();
        for (ParameterDefinition param : params) {
            String name = param.getIdent().getName();
            if (!names.add(name)) {
                return true; // Duplicate found
            }
        }
        return false;
    }

    private List<ParameterDefinition> findUsedParameters(ProcedureDefinition node, List<ParameterDefinition> params) {
        List<ParameterDefinition> used = new ArrayList<>();
        for (ParameterDefinition param : params) {
            if (isParameterUsed(node, param.getIdent().getName())) {
                used.add(param);
            }
        }
        return used;
    }

    private boolean isParameterUsed(ProcedureDefinition node, String paramName) {
        boolean[] found = {false};
        node.accept(new ScratchVisitor() {
            @Override
            public void visit(Parameter param) {
                if (param.getName().getName().equals(paramName)) {
                    found[0] = true;
                }
            }
        });
        return found[0];
    }

    private void applyParameterRename(ProcedureDefinition def, int paramIndex, String oldName, String newName) {
        // Update parameter definition
        List<ParameterDefinition> params = def.getParameterDefinitionList().getParameterDefinitions();
        ParameterDefinition oldParam = params.get(paramIndex);
        ParameterDefinition renamedParam = new ParameterDefinition(
                new StrId(newName), oldParam.getType(), oldParam.getMetadata());
        renamedParam.setParentNode(def.getParameterDefinitionList());
        params.set(paramIndex, renamedParam);

        // Rename all references in the body - modify in place
        // Clone each statement in the body with renamed parameters and replace
        StmtList body = def.getStmtList();
        List<Stmt> originalStmts = new ArrayList<>(body.getStmts());
        body.getStmts().clear();

        ParameterRenamer renamer = new ParameterRenamer(oldName, newName);
        for (Stmt stmt : originalStmts) {
            Stmt renamedStmt = renamer.apply(stmt);
            renamedStmt.setParentNode(body);
            body.getStmts().add(renamedStmt);
        }
    }

    /**
     * Helper CloneVisitor that renames all Parameter references from oldName to newName.
     *
     * <p>
     * This is used in conjunction with {@link #modifyClonedNode} to deep-rename parameters
     * within the procedure body. We use the standard modifyClonedNode for the outer mutation,
     * but need this helper for the nested renaming operation.
     */
    private static class ParameterRenamer extends CloneVisitor {
        private final String oldName;
        private final String newName;

        ParameterRenamer(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public ASTNode visit(Parameter node) {
            if (node.getName().getName().equals(oldName)) {
                return new Parameter(new StrId(newName), node.getType(), node.getMetadata());
            }
            return super.visit(node);
        }
    }
}

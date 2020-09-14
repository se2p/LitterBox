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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;

import java.util.LinkedList;
import java.util.Set;

/**
 * The parameter names in custom blocks do not have to be unique.
 * Therefore, when two parameters have the same name, no matter the type or which one is used inside the custom
 * block, it will always be evaluated as the last input to the block. The parameters have to
 * be used to be counted.
 */
public class AmbiguousParameterNameUsed extends AbstractIssueFinder {
    public static final String NAME = "ambiguous_parameter_name_used";
    private boolean inStmtList = false;
    private boolean found = false;
    private boolean used = false;
    private LinkedList<String> paraNames = new LinkedList<>();

    @Override
    public Set<Issue> check(Program program) {
        found = false;
        return super.check(program);
    }

    private void checkArguments(ArgumentInfo[] arguments) {
        paraNames = new LinkedList<>();

        for (int i = 0; i < arguments.length; i++) {
            ArgumentInfo current = arguments[i];
            for (int j = i + 1; j < arguments.length; j++) {
                if (i != j && current.getName().equals(arguments[j].getName())) {
                    if (!paraNames.contains(current.getName())) {
                        paraNames.add(current.getName());
                    }
                    found = true;
                }
            }
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        super.visit(actor);

        if (found) {
            found = false;
        }
    }

    @Override
    public void visit(StmtList node) {
        inStmtList = true;
        visitChildren(node);
        inStmtList = false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        if (node.getStmtList().getStmts().size() > 0) {
            checkArguments(procMap.get(node.getIdent()).getArguments());
        }

        visitChildren(node);

        if (used) {
            addIssue(node, ((ProcedureMetadata) node.getMetadata()).getDefinition());
        }

        // TODO: This handling with used/found seems really error prone
        used = false;
        found = false;
        paraNames.clear();
        currentProcedure = null;
    }

    @Override
    public void visit(StrId node) {
        if (inStmtList && found && paraNames.contains(node.getName())) {
            used = true;
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}

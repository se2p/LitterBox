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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractRefactoringFinder implements RefactoringFinder, ScratchVisitor {

    protected List<Refactoring> refactorings;
    protected ActorDefinition currentActor = null;

    /**
     * Checks the given program for a specific refactoring.
     *
     * @param program The project to check
     * @return a list of instantiated possible refactorings
     */
    @Override
    public List<Refactoring> check(Program program) {
        Preconditions.checkNotNull(program);
        refactorings = new LinkedList<>();
        program.accept(this);
        return Collections.unmodifiableList(refactorings);
    }

    @Override
    public void visit(Program program) {
        // Refactorings are applied only to the code
        visit(program.getActorDefinitionList());
    }

    @Override
    public void visit(ActorDefinition node) {
        this.currentActor = node;
        // Refactorings are applied only to the code
        visit(node.getScripts());
        visit(node.getProcedureDefinitionList());
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof Never) {
            // Only refactor connected blocks
            return;
        }
        visit((ASTNode) node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        // ignore
    }
}

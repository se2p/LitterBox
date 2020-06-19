/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;

/**
 * If a custom block contains a Stop all or Delete this clone and the custom block is called in the
 * middle of another script, the script will never reach the blocks following the call.
 */
public class CustomBlockWithTermination implements ScratchVisitor, IssueFinder {
    public static final String NAME = "custom_block_with_termination";
    public static final String SHORT_NAME = "custBlWithTerm";
    public static final String HINT_TEXT = "custom block with termination";
    private static final String NOTE1 = "There are no custom blocks with termination where the call is followed by " +
            "statements in your project.";
    private static final String NOTE2 = "Some of the sprites contain custom blocks with forever where the call is " +
            "followed by statements.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private String currentProcedureName;
    private List<String> proceduresWithForever;
    private List<CallStmt> calledProcedures;
    private boolean insideProcedure;
    private Map<LocalIdentifier, ProcedureInfo> procMap;
    private Program program;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        this.program = program;
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        calledProcedures = new ArrayList<>();
        proceduresWithForever = new ArrayList<>();
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
        checkCalls();
        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    private void checkCalls() {
        for (CallStmt calledProcedure : calledProcedures) {
            if (proceduresWithForever.contains(calledProcedure.getIdent().getName())) {
                found = true;
                count++;
                addBlockComment((NonDataBlockMetadata) calledProcedure.getMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + count);
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();

        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(DeleteClone node) {
        if (insideProcedure) {
            proceduresWithForever.add(currentProcedureName);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(StopAll node) {
        if (insideProcedure) {
            proceduresWithForever.add(currentProcedureName);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size() - 1; i++) {
            if (stmts.get(i) instanceof CallStmt) {
                calledProcedures.add((CallStmt) stmts.get(i));
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}

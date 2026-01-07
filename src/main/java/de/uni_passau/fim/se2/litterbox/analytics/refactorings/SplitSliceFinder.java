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
package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitSlice;

import java.util.*;

public class SplitSliceFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        ControlFlowGraph cfg = getControlFlowGraphForScript(script);
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        // Remove entry, exit, and event node
        pdg.removeNode(cfg.getEntryNode());
        pdg.removeNode(cfg.getExitNode());
        pdg.removeNode(script.getEvent());

        Set<Stmt> visited = new HashSet<>();
        List<Stmt> reverseOrderedStmts = ControlFlowGraph.getStatementsInReverseOrder(script.getStmtList());
        Set<Set<Stmt>> slices = new LinkedHashSet<>();

        for (Stmt stmt : reverseOrderedStmts) {
            if (visited.contains(stmt)) {
                continue;
            }

            Set<Stmt> newSlice = pdg.backwardSlice(stmt);

            // Check for overlaps and merge
            Iterator<Set<Stmt>> it = slices.iterator();
            while (it.hasNext()) {
                Set<Stmt> existingSlice = it.next();
                if (overlaps(newSlice, existingSlice)) {
                    newSlice.addAll(existingSlice);
                    it.remove();
                }
            }

            slices.add(newSlice);
            visited.addAll(newSlice);
        }

        List<List<Stmt>> reconstructedSlices = new ArrayList<>();
        List<Stmt> scriptStmts = script.getStmtList().getStmts();
        for (Set<Stmt> slice : slices) {
            List<Stmt> reconstructed = reconstruct(scriptStmts, slice);
            if (!reconstructed.isEmpty()) {
                reconstructedSlices.add(reconstructed);
            }
        }

        if (reconstructedSlices.size() > 1) {
            refactorings.add(new SplitSlice(script, new LinkedHashSet<>(reconstructedSlices)));
        }
    }

    private List<Stmt> reconstruct(List<Stmt> originalStmts, Set<Stmt> slice) {
        List<Stmt> result = new ArrayList<>();
        for (Stmt stmt : originalStmts) {
            if (slice.contains(stmt)) {
                if (stmt instanceof RepeatForeverStmt repeatForeverStmt) {
                    StmtList body = new StmtList(reconstruct(repeatForeverStmt.getStmtList().getStmts(), slice));
                    result.add(new RepeatForeverStmt(body, repeatForeverStmt.getMetadata()));
                } else if (stmt instanceof RepeatTimesStmt repeatTimesStmt) {
                    StmtList body = new StmtList(reconstruct(repeatTimesStmt.getStmtList().getStmts(), slice));
                    result.add(new RepeatTimesStmt(repeatTimesStmt.getTimes(), body, repeatTimesStmt.getMetadata()));
                } else if (stmt instanceof UntilStmt untilStmt) {
                    StmtList body = new StmtList(reconstruct(untilStmt.getStmtList().getStmts(), slice));
                    result.add(new UntilStmt(untilStmt.getBoolExpr(), body, untilStmt.getMetadata()));
                } else if (stmt instanceof IfThenStmt ifThenStmt) {
                    StmtList body = new StmtList(reconstruct(ifThenStmt.getThenStmts().getStmts(), slice));
                    result.add(new IfThenStmt(ifThenStmt.getBoolExpr(), body, ifThenStmt.getMetadata()));
                } else if (stmt instanceof IfElseStmt ifElseStmt) {
                    StmtList thenBody = new StmtList(reconstruct(ifElseStmt.getThenStmts().getStmts(), slice));
                    StmtList elseBody = new StmtList(reconstruct(ifElseStmt.getElseStmts().getStmts(), slice));

                    if (!thenBody.getStmts().isEmpty() && !elseBody.getStmts().isEmpty()) {
                        result.add(
                                new IfElseStmt(ifElseStmt.getBoolExpr(), thenBody, elseBody, ifElseStmt.getMetadata())
                        );
                    } else if (!thenBody.getStmts().isEmpty()) {
                        result.add(new IfThenStmt(ifElseStmt.getBoolExpr(), thenBody, ifElseStmt.getMetadata()));
                    } else if (!elseBody.getStmts().isEmpty()) {
                        BoolExpr negated = new Not(
                                ifElseStmt.getBoolExpr(), NonDataBlockMetadata.emptyNonBlockMetadata()
                        );
                        result.add(new IfThenStmt(negated, elseBody, ifElseStmt.getMetadata()));
                    }
                } else {
                    result.add(stmt);
                }
            }
        }
        return result;
    }

    private boolean overlaps(Set<Stmt> slice1, Set<Stmt> slice2) {
        for (Stmt s : slice1) {
            if (slice2.contains(s)
                    && !(s instanceof ControlStmt)
                    && !(s instanceof de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return SplitSlice.NAME;
    }
}

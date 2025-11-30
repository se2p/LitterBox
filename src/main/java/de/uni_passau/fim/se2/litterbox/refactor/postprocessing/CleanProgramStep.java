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
package de.uni_passau.fim.se2.litterbox.refactor.postprocessing;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CleanProgramStep implements PostProcessorStep {
    private static final Logger log = Logger.getLogger(CleanProgramStep.class.getName());

    @Override
    public List<RefactorSequence> apply(List<RefactorSequence> solutions, PostProcessorContext context) {
        for (RefactorSequence sequence : solutions) {
            Program program = sequence.getRefactoredProgram();
            cleanProgram(program);
        }
        return solutions;
    }

    private String cleanProgram(Program program) {
        int removedDeadScripts = 0;
        int removedControlOnlyScripts = 0;
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            int[] removed = cleanScripts(actor.getScripts());
            removedDeadScripts += removed[0];
            removedControlOnlyScripts += removed[1];
        }
        String report = "Removed " + removedDeadScripts + " dead scripts, Removed " + removedControlOnlyScripts + " control-only scripts";
        log.info(report);
        return report;
    }

    private int[] cleanScripts(ScriptList scriptList) {
        List<Script> scriptsToRemove = new ArrayList<>();
        int removedDeadScripts = 0;
        int removedControlOnlyScripts = 0;
        for (Script script : scriptList.getScriptList()) {
            if (script.getEvent() instanceof Never) {
                scriptsToRemove.add(script);
                removedDeadScripts++;
            } else if (isOnlyControl(script.getStmtList())) {
                scriptsToRemove.add(script);
                removedControlOnlyScripts++;
            }
        }
        scriptList.getScriptList().removeAll(scriptsToRemove);
        return new int[]{removedDeadScripts, removedControlOnlyScripts};
    }

    private boolean isOnlyControl(StmtList stmtList) {
        if (!stmtList.hasStatements()) {
            return true;
        }

        for (Stmt stmt : stmtList.getStmts()) {
            if (!(stmt instanceof ControlStmt)) {
                return false;
            }
            
            // It is a ControlStmt, check its children
            if (!checkControlStmtChildren((ControlStmt) stmt)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkControlStmtChildren(ControlStmt stmt) {
        if (stmt instanceof IfElseStmt) {
            IfElseStmt ifElseStmt = (IfElseStmt) stmt;
            if (!isOnlyControl(ifElseStmt.getThenStmts())) {
                return false;
            }
            if (!isOnlyControl(ifElseStmt.getElseStmts())) {
                return false;
            }
            return true;
        }

        if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;
            return isOnlyControl(ifStmt.getThenStmts());
        }
        
        if (stmt instanceof LoopStmt) {
            LoopStmt loopStmt = (LoopStmt) stmt;
            return isOnlyControl(loopStmt.getStmtList());
        }
        
        return true;
    }
}

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
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScriptDeletionVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CleanProgramStep implements PostProcessorStep {
    private static final Logger log = Logger.getLogger(CleanProgramStep.class.getName());

    @Override
    public List<RefactorSequence> apply(List<RefactorSequence> solutions, PostProcessorContext context) {
        List<RefactorSequence> cleanedSolutions = new ArrayList<>();
        for (RefactorSequence sequence : solutions) {
            Program program = sequence.getRefactoredProgram();
            Program cleanedProgram = cleanProgram(program);
            if (program != cleanedProgram) {
                RefactorSequence newSeq = new RefactorSequence(
                        cleanedProgram,
                        Mutation.identity(),
                        Crossover.identity(),
                        Collections.emptyList()
                );
                for (Map.Entry<FitnessFunction<RefactorSequence>, Double> entry : sequence.getFitnessMap().entrySet()) {
                    newSeq.getFitnessMap().put(entry.getKey(), entry.getValue());
                }
                cleanedSolutions.add(newSeq);
            } else {
                cleanedSolutions.add(sequence);
            }
        }
        return cleanedSolutions;
    }

    private Program cleanProgram(Program program) {
        Set<Script> scriptsToRemove = new HashSet<>();
        int removedDeadScripts = 0;
        int removedControlOnlyScripts = 0;
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            Pair<Integer> removed = cleanScripts(actor.getScripts(), scriptsToRemove);
            removedDeadScripts += removed.getFst();
            removedControlOnlyScripts += removed.getSnd();
        }

        if (!scriptsToRemove.isEmpty()) {
            log.info(
                    "Removed " + removedDeadScripts + " dead scripts, Removed "
                    + removedControlOnlyScripts + " control-only scripts"
            );
            ScriptDeletionVisitor visitor = new ScriptDeletionVisitor(scriptsToRemove);
            return (Program) program.accept(visitor);
        }
        return program;
    }

    private Pair<Integer> cleanScripts(ScriptList scriptList, Set<Script> scriptsToRemove) {
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
        return new Pair<>(removedDeadScripts, removedControlOnlyScripts);
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
        if (stmt instanceof IfElseStmt ifElseStmt) {
            if (!isOnlyControl(ifElseStmt.getThenStmts())) {
                return false;
            }
            if (!isOnlyControl(ifElseStmt.getElseStmts())) {
                return false;
            }
            return true;
        }

        if (stmt instanceof IfStmt ifStmt) {
            return isOnlyControl(ifStmt.getThenStmts());
        }

        if (stmt instanceof LoopStmt loopStmt) {
            return isOnlyControl(loopStmt.getStmtList());
        }

        return true;
    }
}

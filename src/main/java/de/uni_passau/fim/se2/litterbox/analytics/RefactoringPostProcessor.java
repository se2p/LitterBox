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
package de.uni_passau.fim.se2.litterbox.analytics;

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
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import java.util.List;
import java.util.Set;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.Dominance;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

public class RefactoringPostProcessor {

    private static final Logger log = Logger.getLogger(RefactoringPostProcessor.class.getName());

    private final List<FitnessFunction<RefactorSequence>> fitnessFunctions;

    public RefactoringPostProcessor(List<FitnessFunction<RefactorSequence>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    public void process(List<RefactorSequence> solutions, Program originalProgram, Path outputDir, Path originalFile) throws IOException {
        if (solutions.isEmpty()) {
            log.info("No solutions found");
            return;
        }

        log.info("Found " + solutions.size() + " solutions");

        List<RefactorSequence> uniqueSolutions = solutions
                 .stream()
                 .filter(s -> !StructuralEquality.areStructurallyEqual(s.getRefactoredProgram(), originalProgram))
                 .toList();
         log.info("After filtering unchanged programs: " + uniqueSolutions.size());

        uniqueSolutions = uniqueSolutions
                .stream()
                .filter(distinctByFitness(RefactorSequence::getFitnessMap))
                .toList();
        log.info("After filtering identical fitness values: " + uniqueSolutions.size());

        Dominance<RefactorSequence> dominance = new Dominance<>(fitnessFunctions);

        // Create an empty sequence representing the original program
        RefactorSequence original = new RefactorSequence(
                originalProgram,
                uniqueSolutions.get(0).getMutation(),
                uniqueSolutions.get(0).getCrossover(),
                Collections.emptyList()
        );

        // Filter solutions that dominate the original program
        List<RefactorSequence> dominatingSolutions = uniqueSolutions.stream()
                .filter(solution -> dominance.test(solution, original))
                .toList();

        log.info("Dominating solutions: " + dominatingSolutions.size());

        if (dominatingSolutions.isEmpty()) {
            log.info("No solutions found that dominate the original program.");
            return;
        }


        List<Program> processedPrograms = new ArrayList<>();

        int duplicateOthers = 0;
        
        for (RefactorSequence sequence : dominatingSolutions) {
            Program program = sequence.getRefactoredProgram(); // This is already a copy/new instance

            String cleaningReport = cleanProgram(program);
            //log.info(cleaningReport);

            // Check for structural equality against already processed programs
            boolean isDuplicate = false;
            for (Program existing : processedPrograms) {
                if (StructuralEquality.areStructurallyEqual(existing, program)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate) {
                processedPrograms.add(program);
            } else {
                duplicateOthers++;
            }
        }
        log.info("Removed " + duplicateOthers + " solutions identical to other solutions.");

        log.info("Unique solutions: " + processedPrograms.size());

        if (!outputDir.toFile().exists()) {
            Files.createDirectories(outputDir);
        }

        for (int i = 0; i < processedPrograms.size(); i++) {
            Program program = processedPrograms.get(i);
            String suffix = "_refactored_" + i;
            if (FilenameUtils.getExtension(originalFile.toString()).equalsIgnoreCase("json")) {
                JSONFileCreator.writeJsonFromProgram(program, outputDir, suffix);
            } else {
                JSONFileCreator.writeSb3FromProgram(program, outputDir, originalFile.toFile(), suffix);
            }
            log.info("Written solution to " + outputDir.resolve(FilenameUtils.getBaseName(originalFile.toString()) + suffix + "." + FilenameUtils.getExtension(originalFile.toString())));
        }
    }

    public static <T> Predicate<T> distinctByFitness(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private String cleanProgram(Program program) {
        int removedDeadScripts = 0;
        int removedControlOnlyScripts = 0;
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            int[] removed = cleanScripts(actor.getScripts());
            removedDeadScripts += removed[0];
            removedControlOnlyScripts += removed[1];
        }
        return "Removed " + removedDeadScripts + " dead scripts, Removed " + removedControlOnlyScripts + " control-only scripts";
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
        
        // If it's a ControlStmt but not one of the above (e.g. maybe some other type?), 
        // we assume it might be empty or we should check if there are other types.
        // Based on file list: ControlStmt, IfElseStmt, IfStmt, IfThenStmt, LoopStmt, RepeatForeverStmt, RepeatTimesStmt, UntilStmt.
        // IfThenStmt likely implements IfStmt.
        // RepeatForever, RepeatTimes, Until likely implement LoopStmt.
        // So this should cover it.
        
        return true;
    }
}

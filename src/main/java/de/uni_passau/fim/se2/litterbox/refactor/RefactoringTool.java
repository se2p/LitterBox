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
package de.uni_passau.fim.se2.litterbox.refactor;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.*;

import java.util.*;

/**
 * Holds all Refactorings and executes them.
 * Register new implemented refactorings here.
 */
public final class RefactoringTool {

    private RefactoringTool() {
    }

    private static Map<String, RefactoringFinder> generateRefactoringFinders() {
        Map<String, RefactoringFinder> refactorings = new LinkedHashMap<>();

        registerRefactoring(new ConjunctionToIfsFinder(), refactorings); // ifs to conjunction backwards
        registerRefactoring(new ConjunctionToIfElseFinder(), refactorings); // if if else to conjunction backwards
        registerRefactoring(new DisjunctionToIfElseFinder(), refactorings); // if else to disjunction backwards
        registerRefactoring(new ExtractLoopConditionFinder(), refactorings); // transform loop condition
        registerRefactoring(new ExtractEventsFromForeverFinder(), refactorings); // extract events from forever
        registerRefactoring(new ForeverIfToWaitUntilFinder(), refactorings); // forever if to wait until
        registerRefactoring(new ForeverWaitToForeverIfFinder(), refactorings); // forever if to wait until backwards
        registerRefactoring(new IfsToConjunctionFinder(), refactorings); // ifs to conjunction
        registerRefactoring(new IfIfElseToConjunctionFinder(), refactorings); // if if else to conjunction
        registerRefactoring(new IfElseToDisjunctionFinder(), refactorings); // if else to disjunction
        registerRefactoring(new IfElseToIfIfNotFinder(), refactorings); // if else to if if not
        registerRefactoring(new IfIfNotToIfElseFinder(), refactorings); // if else to if if not backwards
        registerRefactoring(new InlineLoopConditionFinder(), refactorings); // transform loop condition backwards
        registerRefactoring(new LoopUnrollingFinder(), refactorings); // loop unrolling
        registerRefactoring(new MergeDoubleIfFinder(), refactorings); // split if backwards
        registerRefactoring(new MergeEventsIntoForeverFinder(), refactorings); // extract events from forever backwards
        registerRefactoring(new MergeLoopsFinder(), refactorings); // split loop backwards
        registerRefactoring(new MergeScriptsFinder(), refactorings); // split script backwards
        // split script after repeat until backwards
        registerRefactoring(new MergeScriptsAfterUntilFinder(), refactorings);
        registerRefactoring(new SequenceToLoopFinder(), refactorings); // loop unrolling backwards
        registerRefactoring(new SplitIfFinder(), refactorings); // split if body
        registerRefactoring(new SplitLoopFinder(), refactorings); // split loop
        registerRefactoring(new SplitScriptFinder(), refactorings); // split script
        registerRefactoring(new SplitSliceFinder(), refactorings); // extract independent subscripts
        registerRefactoring(new SwapStatementsFinder(), refactorings); // swap statements
        registerRefactoring(new SplitScriptAfterUntilFinder(), refactorings); // split script after repeat until

        return refactorings;
    }

    public static List<RefactoringFinder> getRefactoringFinders() {
        List<RefactoringFinder> refactorings = new ArrayList<>(generateRefactoringFinders().values());
        return Collections.unmodifiableList(refactorings);
    }

    static void registerRefactoring(
            RefactoringFinder refactoringFinder, Map<String, RefactoringFinder> refactoringFinders
    ) {
        refactoringFinders.put(refactoringFinder.getName(), refactoringFinder);
    }
}

/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import java.util.logging.Logger;

/**
 * Holds all Refactorings and executes them.
 * Register new implemented refactorings here.
 */
public class RefactoringTool {

    private RefactoringTool() {
    }

    private static final Logger log = Logger.getLogger(RefactoringTool.class.getName());

    private static Map<String, RefactoringFinder> generateRefactoringFinders() {
        Map<String, RefactoringFinder> refactorings = new LinkedHashMap<>();

        registerRefactoring(new ConjunctionToIfsFinder(), refactorings);
        registerRefactoring(new ConjunctionToIfElseFinder(), refactorings);
        registerRefactoring(new DisjunctionToIfElseFinder(), refactorings);
        registerRefactoring(new MergeDoubleIfFinder(), refactorings);
        registerRefactoring(new DoubleEventFinder(), refactorings);
        registerRefactoring(new IfsToConjunctionFinder(), refactorings);
        registerRefactoring(new IfElseToConjunctionFinder(), refactorings);
        registerRefactoring(new IfElseToDisjunctionFinder(), refactorings);
        registerRefactoring(new SemanticScriptFinder(), refactorings);
        registerRefactoring(new SplitIfFinder(), refactorings);

        return refactorings;
    }

    public static List<RefactoringFinder> getRefactoringFinders() {
        List<RefactoringFinder> refactorings = new ArrayList<>(generateRefactoringFinders().values());
        return Collections.unmodifiableList(refactorings);
    }

    static void registerRefactoring(RefactoringFinder refactoringFinder, Map<String, RefactoringFinder> refactoringFinders) {
        refactoringFinders.put(refactoringFinder.getName(), refactoringFinder);
    }
}

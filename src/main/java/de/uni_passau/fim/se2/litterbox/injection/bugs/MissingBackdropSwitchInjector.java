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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingBackdropSwitch;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injector for the {@link MissingBackdropSwitch} bug.
 *
 * <p>
 * The MissingBackdropSwitch bug occurs when a "when backdrop switches to" event handler
 * exists, but no SwitchBackdrop/SwitchBackdropAndWait statement switches to that backdrop.
 *
 * <p>
 * This injector removes SwitchBackdrop/SwitchBackdropAndWait statements that switch to
 * backdrops for which BackdropSwitchTo event handlers exist, creating a dead handler.
 *
 * <p>
 * Precondition: The bug is only detected if there are no NextBackdrop, Previous, or Random
 * backdrop blocks in the program (since those could potentially trigger any backdrop).
 */
public class MissingBackdropSwitchInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MissingBackdropSwitch();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program node) {
        // Precondition: Check if any NextBackdrop, Prev, or Random blocks exist
        if (hasNextPrevRandomBlocks(node)) {
            // Cannot inject - the precondition fails
            return;
        }

        // Collect all backdrop names that have BackdropSwitchTo event handlers
        Set<String> handledBackdrops = collectBackdropSwitchToNames(node);
        if (handledBackdrops.isEmpty()) {
            return;
        }

        // Collect backdrop names that have both handlers AND switches
        var backdropNamesWithSwitches = collectBackdropsWithSwitches(node, handledBackdrops);

        // For each backdrop, create a mutation that removes ALL switches to that backdrop
        for (String backdropName : backdropNamesWithSwitches) {
            // Find the first switch for this backdrop to use as mutation anchor
            Stmt firstSwitch = findFirstSwitchForBackdrop(node, backdropName);
            if (firstSwitch != null) {
                modifyClonedNode(firstSwitch, clonedSwitch -> {
                    // Navigate from the cloned switch to find its program root
                    Program clonedProgram = findProgramRoot(clonedSwitch);
                    if (clonedProgram != null) {
                        // Remove ALL switches to this backdrop from the cloned program
                        removeAllSwitchesToBackdrop(clonedProgram, backdropName);
                    }
                });
            }
        }
    }

    /**
     * Collects backdrop names that have handlers and have at least one switch.
     */
    private Set<String> collectBackdropsWithSwitches(Program program, Set<String> handledBackdrops) {
        Set<String> result = new HashSet<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(SwitchBackdrop node) {
                String backdropName = extractBackdropName(node.getElementChoice());
                if (backdropName != null && handledBackdrops.contains(backdropName.toLowerCase())) {
                    result.add(backdropName.toLowerCase());
                }
            }

            @Override
            public void visit(SwitchBackdropAndWait node) {
                String backdropName = extractBackdropName(node.getElementChoice());
                if (backdropName != null && handledBackdrops.contains(backdropName.toLowerCase())) {
                    result.add(backdropName.toLowerCase());
                }
            }
        });
        return result;
    }

    /**
     * Finds the first switch statement for a given backdrop name.
     */
    private Stmt findFirstSwitchForBackdrop(Program program, String backdropName) {
        List<Stmt> found = new ArrayList<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(SwitchBackdrop node) {
                if (found.isEmpty()) {
                    String name = extractBackdropName(node.getElementChoice());
                    if (name != null && name.equalsIgnoreCase(backdropName)) {
                        found.add(node);
                    }
                }
            }

            @Override
            public void visit(SwitchBackdropAndWait node) {
                if (found.isEmpty()) {
                    String name = extractBackdropName(node.getElementChoice());
                    if (name != null && name.equalsIgnoreCase(backdropName)) {
                        found.add(node);
                    }
                }
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    /**
     * Removes all switch statements to the given backdrop from the cloned program.
     */
    private void removeAllSwitchesToBackdrop(Program program, String backdropName) {
        List<Stmt> toRemove = new ArrayList<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(SwitchBackdrop node) {
                String name = extractBackdropName(node.getElementChoice());
                if (name != null && name.equalsIgnoreCase(backdropName)) {
                    toRemove.add(node);
                }
            }

            @Override
            public void visit(SwitchBackdropAndWait node) {
                String name = extractBackdropName(node.getElementChoice());
                if (name != null && name.equalsIgnoreCase(backdropName)) {
                    toRemove.add(node);
                }
            }
        });

        // Remove all found switches from their parent StmtLists
        for (Stmt stmt : toRemove) {
            if (stmt.getParentNode() instanceof StmtList stmtList) {
                stmtList.getStmts().remove(stmt);
            }
        }
    }

    /**
     * Extracts the backdrop name from an ElementChoice, or null if it's Next/Prev/Random/NumExpr.
     */
    private String extractBackdropName(ElementChoice choice) {
        if (choice instanceof WithExpr withExpr) {
            if (withExpr.getExpression() instanceof StrId strId) {
                return strId.getName();
            } else if (withExpr.getExpression() instanceof StringLiteral lit) {
                return lit.getText();
            } else if (withExpr.getExpression() instanceof AsString asString) {
                if (asString.getOperand1() instanceof StrId strId) {
                    return strId.getName();
                } else if (asString.getOperand1() instanceof StringLiteral lit) {
                    return lit.getText();
                }
            }
        }
        return null;
    }

    /**
     * Checks if the program contains any NextBackdrop, Prev, Random, or NumExpr-based backdrop blocks.
     * If so, the MissingBackdropSwitch bug cannot be reliably detected.
     */
    private boolean hasNextPrevRandomBlocks(Program program) {
        NextPrevRandomChecker checker = new NextPrevRandomChecker();
        program.accept(checker);
        return checker.hasNextPrevRandom();
    }

    /**
     * Collects all backdrop names that have BackdropSwitchTo event handlers.
     */
    private Set<String> collectBackdropSwitchToNames(Program program) {
        Set<String> names = new HashSet<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(BackdropSwitchTo node) {
                names.add(node.getBackdrop().getName().toLowerCase());
            }
        });
        return names;
    }

    /**
     * Visitor that checks for NextBackdrop, Prev, Random, or NumExpr-based backdrop switches.
     */
    private static class NextPrevRandomChecker implements ScratchVisitor {
        private boolean found = false;

        boolean hasNextPrevRandom() {
            return found;
        }

        @Override
        public void visit(NextBackdrop node) {
            found = true;
        }

        @Override
        public void visit(SwitchBackdrop node) {
            checkElementChoice(node.getElementChoice());
        }

        @Override
        public void visit(SwitchBackdropAndWait node) {
            checkElementChoice(node.getElementChoice());
        }

        private void checkElementChoice(ElementChoice choice) {
            if (choice instanceof Next || choice instanceof Prev || choice instanceof Random) {
                found = true;
            } else if (choice instanceof WithExpr withExpr && withExpr.getExpression() instanceof NumExpr) {
                found = true;
            }
        }
    }
}

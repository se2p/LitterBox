/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TypeError;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Optional;
import java.util.Set;

/**
 * Fix heuristic for the TypeError bug.
 *
 * <p>Uses composition with TypeError bug finder to check if the type error still exists.
 * The bug is fixed if the node exists but TypeError no longer reports it.
 */
public class TypeErrorFix extends AbstractFixHeuristic {

    private final TypeError typeErrorFinder;

    public TypeErrorFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
        this.typeErrorFinder = new TypeError();
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return typeErrorFinder;
    }

    @Override
    public Set<Issue> check(Program program) {
        // Run the bug finder
        Set<Issue> foundIssues = typeErrorFinder.check(program);

        // Find the target node
        Optional<BlockIdNodeFinder.NodeContext> contextOpt =
                BlockIdNodeFinder.find(program, bugLocationBlockId);

        if (contextOpt.isEmpty()) {
            return issues;
        }

        contextOpt.ifPresent(context -> {
            ASTNode foundNode = context.node();
            boolean bugPersists = false;

            for (Issue issue : foundIssues) {
                if (isSelfOrDescendant(issue.getCodeLocation(), foundNode)) {
                    bugPersists = true;
                    break;
                }
            }

            if (!bugPersists) {
                IssueBuilder builder = new IssueBuilder()
                        .withFinder(this)
                        .withCurrentNode(foundNode)
                        .withActor(context.actor())
                        .withProgram(program)
                        .withMetadata(foundNode.getMetadata())
                        .withHint(Hint.fromKey(getName()));

                if (context.script() != null) {
                    builder.withScript(context.script());
                } else if (context.procedure() == null) {
                    // Fallback: try to find script parent manually
                    de.uni_passau.fim.se2.litterbox.ast.model.Script script = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.findParent(foundNode, de.uni_passau.fim.se2.litterbox.ast.model.Script.class);
                    if (script != null) {
                        builder.withScript(script);
                    }
                }

                if (context.procedure() != null) {
                    builder.withProcedure(context.procedure());
                } else {
                    // Fallback: try to find procedure parent manually if script is also missing (or even if script found, procedure might be parent?)
                    // Actually, if procedure is found, script is usually null.
                    de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition proc = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.findParent(foundNode, de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition.class);
                    if (proc != null) {
                        builder.withProcedure(proc);
                    }
                }

                issues.add(builder.build());
            }
        });

        return issues;
    }

    private boolean isSelfOrDescendant(ASTNode node, ASTNode potentialParent) {
        ASTNode curr = node;
        while (curr != null) {
            if (curr == potentialParent) {
                return true;
            }
            curr = curr.getParentNode();
        }
        return false;
    }
}

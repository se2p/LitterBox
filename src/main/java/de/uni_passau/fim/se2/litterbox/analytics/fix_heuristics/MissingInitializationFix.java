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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Fix heuristic for the MissingInitialization bug.
 *
 * <p>The bug is reported when a variable is used before it is initialized.
 *
 * <p>The bug is fixed if the variable use is now properly initialized before use.
 *
 * <p><b>Note:</b> Multiple MissingInitialization bugs can exist at the same block ID
 * for different variables (e.g., x-position and y-position). This heuristic uses
 * the hint to distinguish between them, detecting a fix only when the SPECIFIC
 * variable's initialization is no longer missing.
 */
public class MissingInitializationFix extends AbstractFixHeuristic {

    private final MissingInitialization relatedBugFinder = new MissingInitialization();

    public MissingInitializationFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        this.program = program;
        this.fixes = new LinkedHashSet<>();
        resetState();

        String blockId = getBugLocationBlockId();
        String originalHint = getOriginalIssue().hint();

        if (blockId == null || blockId.isBlank()) {
            return Collections.emptySet();
        }

        Optional<BlockIdNodeFinder.NodeContext> contextOpt = BlockIdNodeFinder.find(program, blockId);

        if (contextOpt.isEmpty()) {
            // Block deleted - valid fix
            addFixWithLooseComment();
        } else {
            // Block exists - check if the SPECIFIC bug (by hint) is still present
            refreshBugInstances(program);
            BlockIdNodeFinder.NodeContext context = contextOpt.get();

            if (!isSameBugStillPresent(blockId, originalHint)) {
                // Fixed! The specific variable initialization is no longer missing.
                currentActor = context.actor();
                currentScript = context.script();
                currentProcedure = context.procedure();
                addFix(context.node(), context.node().getMetadata());
            }
        }

        return Collections.unmodifiableSet(fixes);
    }

    /**
     * Checks if the same bug (matching by hint) is still present in the fixed version.
     *
     * <p>If no hint was provided in the original issue, falls back to block ID matching.
     *
     * @param blockId      the original block ID
     * @param originalHint the hint from the original issue (containing the variable name)
     * @return true if the same bug is still present
     */
    private boolean isSameBugStillPresent(String blockId, String originalHint) {
        for (Issue issue : currentBugInstances) {
            String issueBockId = AstNodeUtil.getBlockId(issue.getCodeLocation());

            // Must match the block ID
            if (!blockId.equals(issueBockId)) {
                continue;
            }

            // If we have a hint, check if the hint matches (same variable)
            if (originalHint != null && !originalHint.isBlank()) {
                String issueHint = issue.getHint() != null
                        ? issue.getHint().getHintText(IssueTranslatorFactory.getIssueTranslator())
                        : "";
                if (hintsMatch(originalHint, issueHint)) {
                    return true;
                }
            } else {
                // No hint provided - any bug at this block counts as persisting
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two hints match, indicating the same variable is affected.
     *
     * <p>Uses exact string matching to compare hints. MissingInitialization hints
     * contain the variable/attribute name in a format like:
     * "missing_initialization{variable=x position}"
     */
    private boolean hintsMatch(String hint1, String hint2) {
        if (hint1 == null || hint2 == null) {
            return false;
        }
        return hint1.equals(hint2);
    }
}


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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Represents the result of a fix heuristic check.
 *
 * <p>A FixResult indicates that a previously reported bug has been resolved.
 * Unlike {@link de.uni_passau.fim.se2.litterbox.analytics.Issue}, which represents
 * a problem, a FixResult represents the confirmation that a problem was fixed.
 *
 * <p><b>Nullability:</b>
 * <ul>
 *   <li>{@code fixedNode} - nullable: null if block was deleted (deletion is a valid fix)</li>
 *   <li>{@code actor} - nullable: null for program-level issues or loose fixes</li>
 *   <li>{@code script} - nullable: null for actor-level bugs with no specific script</li>
 *   <li>{@code metadata} - nullable: null if fix is inferred without specific metadata</li>
 * </ul>
 *
 * @param program       the program that was analyzed (never null)
 * @param fixedNode     the AST node where the fix was detected, or null if deleted
 * @param originalIssue the original issue that was fixed (never null)
 * @param actor         the actor containing the fixed code, or null
 * @param script        the script or procedure containing the fixed code, or null
 * @param metadata      the metadata associated with the fixed node, or null
 */
public record FixResult(
        Program program,
        /* nullable */ ASTNode fixedNode,
        IssueDTO originalIssue,
        /* nullable */ ActorDefinition actor,
        /* nullable */ ScriptEntity script,
        /* nullable */ Metadata metadata
) {
    /**
     * Returns the name of the bug finder that originally reported the bug.
     */
    public String getOriginalBugFinder() {
        return originalIssue.finder();
    }

    /**
     * Returns the block ID where the original bug was located.
     */
    public String getBugLocationBlockId() {
        return originalIssue.issueLocationBlockId();
    }

    /**
     * Returns the name of the actor containing the fixed code.
     *
     * @return the actor name, or null if no actor context
     */
    public String getActorName() {
        return actor != null ? actor.getIdent().getName() : null;
    }

    /**
     * Returns the scratchblocks code representation of the original issue location.
     *
     * @return the scratchblocks code, or null if not available
     */
    public String getOriginalScratchBlocksCode() {
        return originalIssue.scratchBlocksCode();
    }
}

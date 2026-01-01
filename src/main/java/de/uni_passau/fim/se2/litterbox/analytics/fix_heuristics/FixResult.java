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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
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
 * @param program the program that was analyzed
 * @param fixedNode the AST node where the fix was detected
 * @param originalIssue the original issue that was fixed (contains bug finder info, block ID, etc.)
 * @param actor the actor containing the fixed code
 * @param script the script or procedure containing the fixed code
 * @param metadata the metadata associated with the fixed node
 */
public record FixResult(
        Program program,
        ASTNode fixedNode,
        IssueDTO originalIssue,
        ActorDefinition actor,
        ScriptEntity script,
        Metadata metadata
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
     */
    public String getActorName() {
        return actor != null ? actor.getIdent().getName() : null;
    }
}

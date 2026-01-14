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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.FixResult;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Converts between different issue representations (Issue, IssueDTO, FixResult).
 */
public final class IssueConverter {

    private static final Logger log = Logger.getLogger(IssueConverter.class.getName());

    private IssueConverter() {
        // Utility class
    }

    /**
     * Converts FixResults to Issue objects for backward compatibility.
     *
     * @param fixResults the fix results to convert
     * @param program    the program context
     * @return list of Issue objects representing the fixes
     */
    public static List<Issue> convertFixResultsToIssues(List<FixResult> fixResults, Program program) {
        List<Issue> issues = new ArrayList<>();
        List<ActorDefinition> actors = program.getActorDefinitionList().getDefinitions();

        for (FixResult fix : fixResults) {
            ActorDefinition actor = fix.actor();
            ASTNode node = fix.fixedNode();

            // Handle null actor - look up by the original issue's actor name
            if (actor == null && fix.originalIssue() != null && fix.originalIssue().sprite() != null) {
                actor = findActorByName(actors, fix.originalIssue().sprite());
            }

            // Final fallback to first actor if still null
            if (actor == null && !actors.isEmpty()) {
                actor = actors.getFirst();
            }

            if (actor == null) {
                log.fine(() -> "Skipping fix result with no actor context: " + fix.getOriginalBugFinder());
                continue;
            }

            Issue issue = buildFixIssue(fix, program, actor, node);
            issues.add(issue);
        }
        return issues;
    }

    private static Issue buildFixIssue(FixResult fix, Program program, ActorDefinition actor, ASTNode node) {
        // Determine code snippet - prefer from original issue, fall back to generating from node
        String codeSnippet = fix.getOriginalScratchBlocksCode();
        if ((codeSnippet == null || codeSnippet.isBlank()) && node != null) {
            codeSnippet = ScratchBlocksVisitor.of(node);
        }

        IssueBuilder builder = new IssueBuilder()
                .withFinder(new SyntheticIssueFinder(fix.getOriginalBugFinder(), IssueType.FIX))
                .withProgram(program)
                .withActor(actor)
                .withMetadata(fix.metadata())
                .withSeverity(IssueSeverity.LOW)
                .withHint(Hint.fromText(fix.originalIssue().hint()))
                .withOriginalBlockId(fix.getBugLocationBlockId())
                .withOriginalCodeSnippet(codeSnippet);

        // IssueBuilder requires both currentNode and script to be set, or both null
        if (node != null) {
            ScriptEntity script = fix.script();
            if (script == null) {
                script = AstNodeUtil.findParent(node, ScriptEntity.class);
            }
            if (script != null) {
                builder.withCurrentNode(node).withScriptOrProcedure(script);
            }
        }

        return builder.build();
    }

    /**
     * Creates Issue objects for removed issues (issues that disappeared but couldn't be verified as fixed).
     *
     * @param removedInfos the removed issue information
     * @param program      the program context
     * @return list of Issue objects representing removed issues
     */
    public static List<Issue> createRemovedIssues(
            List<IssueCategorizer.RemovedIssueInfo> removedInfos,
            Program program
    ) {
        List<Issue> removed = new ArrayList<>();
        List<ActorDefinition> actors = program.getActorDefinitionList().getDefinitions();

        if (actors.isEmpty()) {
            log.warning("Cannot create removed issue records: program has no actors");
            return removed;
        }

        for (IssueCategorizer.RemovedIssueInfo info : removedInfos) {
            ActorDefinition actor = findActorByName(actors, info.actorName());
            IssueFinder removedFinder = new SyntheticIssueFinder(info.finderName(), IssueType.BUG);

            Issue issue = new IssueBuilder()
                    .withFinder(removedFinder)
                    .withProgram(program)
                    .withActor(actor)
                    .withSeverity(IssueSeverity.LOW)
                    .withHint(Hint.fromText(info.hintText()))
                    .withOriginalBlockId(info.blockId())
                    .withOriginalCodeSnippet(info.codeSnippet())
                    .build();
            removed.add(issue);
        }
        return removed;
    }

    /**
     * Creates a minimal IssueDTO from an Issue for comparison purposes.
     *
     * @param issue      the issue to convert
     * @param translator the translator to use for hint text generation
     * @return a minimal IssueDTO with key fields populated
     */
    public static IssueDTO toMinimalDTO(Issue issue, IssueTranslator translator) {
        String blockId = null;
        String scratchBlocksCode = null;
        ASTNode codeLocation = issue.getCodeLocation();
        if (codeLocation != null) {
            ASTNode nodeWithId = AstNodeUtil.findNearestNodeWithBlockId(codeLocation);
            if (nodeWithId != null) {
                blockId = AstNodeUtil.getBlockId(nodeWithId);
            } else {
                blockId = AstNodeUtil.getBlockId(codeLocation);
            }

            ScriptEntity script = AstNodeUtil.findParent(codeLocation, de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity.class);
            ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
            visitor.begin();
            if (script != null) {
                script.accept(visitor);
            } else {
                codeLocation.accept(visitor);
            }
            visitor.end();
            scratchBlocksCode = visitor.getScratchBlocks();
        }

        // Include the hint text for proper issue matching by fix heuristics
        String hintText = null;
        if (issue.getHint() != null) {
            // Use the translated hint text for comparison
            hintText = issue.getHint().getHintText(translator);
        }

        return new IssueDTO(
                issue.getId(),
                issue.getFinderName(),
                issue.getFinderName(),
                issue.getIssueType(),
                issue.getSeverity().ordinal(),
                issue.getActorName(),
                blockId,
                null, null, null, null,
                hintText, null, 0, scratchBlocksCode, null
        );
    }

    private static ActorDefinition findActorByName(List<ActorDefinition> actors, String name) {
        return actors.stream()
                .filter(a -> a.getIdent().getName().equals(name))
                .findFirst()
                .orElse(actors.getFirst());
    }

    /**
     * A synthetic issue finder used for reporting fixed/removed issues.
     */
    private static class SyntheticIssueFinder extends AbstractIssueFinder {
        private final String name;
        private final IssueType type;

        SyntheticIssueFinder(String name, IssueType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public IssueType getIssueType() {
            return type;
        }
    }
}

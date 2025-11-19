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
package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.BlockByIdFinder;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLlm;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

abstract class LLMIssueExtender implements LLMIssueProcessor {

    private static final Logger log = Logger.getLogger(LLMIssueExtender.class.getName());

    private static final String DESCRIPTION_MARKER = "Finding Description:";
    private static final String LOCATION_MARKER = "Finding Location:";

    protected final IssueTranslator translator;

    protected final ScratchLlm scratchLlm;

    protected final LlmApi llmApi;

    protected final PromptBuilder promptBuilder;

    protected final QueryTarget target;

    protected LLMIssueExtender(
            IssueTranslator translator, LlmApi llmApi, PromptBuilder promptBuilder, QueryTarget target
    ) {
        this.translator = translator;
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.scratchLlm = new ScratchLlm(llmApi, promptBuilder);
        this.target = target;
    }

    protected LLMIssueExtender(IssueTranslator translator, QueryTarget target) {
        this(translator, LlmApiProvider.get(), LlmPromptProvider.get(translator), target);
    }

    protected Set<Issue> apply(Program program, Set<Issue> issues, LlmQuery llmQuery, LLMIssueFinder issueFinder) {
        final String prompt = promptBuilder.askQuestion(program, target, llmQuery);
        log.info("Prompt: " + prompt);
        String response = scratchLlm.singleQueryWithTextResponse(prompt);
        log.info("Response: " + response);

        Set<Issue> expandedIssues = new LinkedHashSet<>(issues);
        expandedIssues.addAll(getIssuesFromResponse(response + "\n", program, issueFinder));

        return expandedIssues;
    }

    private Set<Issue> getIssuesFromResponse(String response, Program program, LLMIssueFinder issueFinder) {
        Set<Issue> issues = new LinkedHashSet<>();
        String[] issueEntries = response.split("New Finding( \\d+)?:");
        for (String entry : issueEntries) {
            if (entry.isBlank()) {
                continue;
            }

            tryParseIssue(program, issueFinder, entry).ifPresent(issues::add);
        }

        return issues;
    }

    private Optional<Issue> tryParseIssue(
            final Program program, final LLMIssueFinder issueFinder, final String issueText
    ) {
        if (!issueText.contains(DESCRIPTION_MARKER) || !issueText.contains(LOCATION_MARKER)) {
            log.info("Issue text does not follow the requested format, ignoring: " + issueText);
            return Optional.empty();
        }

        String description = extractValue(issueText, DESCRIPTION_MARKER, LOCATION_MARKER);
        String location = extractValue(issueText, LOCATION_MARKER)
                .replace(ScratchBlocksVisitor.SCRIPT_ID_MARKER, "")
                .trim();

        Optional<ASTNode> node = BlockByIdFinder.findBlock(program, location);
        if (node.isEmpty()) {
            log.warning("Could not find block with ID: " + location);
            return Optional.empty();
        }
        Script script = AstNodeUtil.findParent(node.get(), Script.class);
        if (script == null) {
            log.warning("Could not find script for block with ID: " + location);
            return Optional.empty();
        }
        Optional<ActorDefinition> actor = AstNodeUtil.findActor(node.get());
        if (actor.isEmpty()) {
            log.warning("Could not find actor for block with ID: " + location);
            return Optional.empty();
        }

        Issue issue = new IssueBuilder()
                .withFinder(issueFinder)
                .withSeverity(IssueSeverity.LOW)
                .withHint(Hint.fromText(description))
                .withActor(actor.get())
                .withScriptOrProcedure(script)
                .withCurrentNode(node.orElse(script.getEvent()))
                .withProgram(program)
                .build();
        return Optional.of(issue);
    }

    private String extractValue(String entry, String key) {
        return extractValue(entry, key, "\n");
    }

    private String extractValue(String entry, String startMarker, String endMarker) {
        int startIndex = entry.indexOf(startMarker) + startMarker.length();
        int endIndex = entry.indexOf(endMarker, startIndex);
        return entry.substring(startIndex, endIndex).trim();
    }

}

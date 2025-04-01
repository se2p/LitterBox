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
import de.uni_passau.fim.se2.litterbox.llm.LLMResponseParser;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

abstract class LLMIssueExtender implements LLMIssueProcessor {
    private static final Logger log = Logger.getLogger(LLMIssueExtender.class.getName());

    protected LlmApi llmApi;

    protected PromptBuilder promptBuilder;

    protected QueryTarget target;

    protected LLMIssueExtender(LlmApi llmApi,
                                       PromptBuilder promptBuilder,
                                       QueryTarget target) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.target = target;
    }

    protected LLMIssueExtender(QueryTarget target) {
        this(LlmApiProvider.get(), LlmPromptProvider.get(), target);
    }

    public abstract Set<Issue> apply(Program program, Set<Issue> issues);

    protected Set<Issue> apply(Program program, Set<Issue> issues, LlmQuery llmQuery, LLMIssueFinder issueFinder) {
        final String prompt = promptBuilder.askQuestion(program, target, llmQuery);
        log.info("Prompt: " + prompt);
        String response = LLMResponseParser.fixCommonScratchBlocksIssues(llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text());
        log.info("Response: " + response);

        Set<Issue> expandedIssues = new LinkedHashSet<>(issues);
        expandedIssues.addAll(getIssuesFromResponse(response + "\n", program, issueFinder));

        return expandedIssues;
    }

    private Set<Issue> getIssuesFromResponse(String response, Program program, LLMIssueFinder llmDummyFinder) {
        Set<Issue> issues = new LinkedHashSet<>();
        String[] issueEntries = response.split("New Finding \\d+:");
        for (String entry : issueEntries) {
            if (entry.trim().isEmpty()) continue;

            IssueSeverity severity = IssueSeverity.LOW; // TODO?
            String description = extractValue(entry, "Finding Description:");
            String location = extractValue(entry, "Finding Location:");

            Optional<ASTNode> node = BlockByIdFinder.findBlock(program, location);
            if (!node.isPresent()) {
                log.warning("Could not find block with ID: " + location);
                continue;
            }
            Script script = AstNodeUtil.findParent(node.get(), Script.class);
            if (script == null) {
                log.warning("Could not find script for block with ID: " + location);
                continue;
            }
            Optional<ActorDefinition> actor = AstNodeUtil.findActor(node.get());
            if (!actor.isPresent()) {
                log.warning("Could not find actor for block with ID: " + location);
                continue;
            }

            Issue issue = new IssueBuilder()
                    .withFinder(llmDummyFinder)
                    .withSeverity(severity)
                    .withHint(Hint.fromText(description))
                    .withActor(actor.get())
                    .withScriptOrProcedure(script)
                    .withCurrentNode(script.getEvent()) // TODO: This is likely not the actual node
                    .withProgram(program)
                    .build();

            issues.add(issue);
        }

        return issues;
    }

    private String extractValue(String entry, String key) {
        int startIndex = entry.indexOf(key) + key.length();
        int endIndex = entry.indexOf("\n", startIndex);
        return entry.substring(startIndex, endIndex).trim();
    }

}

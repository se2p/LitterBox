/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.PositionEqualsCheck;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.VariableAsLiteral;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptySprite;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONReportGeneratorTest implements JsonTest {

    private void assertValidJsonIssue(String issueText, int numIssues) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(issueText);
        assertThat(rootNode.has("metrics")).isTrue();
        assertThat(rootNode.has("issues")).isTrue();
        JsonNode issueNode = rootNode.get("issues");
        assertThat(issueNode.size()).isEqualTo(numIssues);

        for (JsonNode node : issueNode) {
            assertThat(node.has("finder")).isTrue();
            assertThat(node.has("name")).isTrue();
            assertThat(node.has("severity")).isTrue();
            assertThat(node.has("type")).isTrue();
            assertThat(node.has("sprite")).isTrue();
            assertThat(node.has("hint")).isTrue();
            assertThat(node.has("code")).isTrue();
            assertThat(node.has("id")).isTrue();
            assertThat(node.has("duplicate-of")).isTrue();
            assertThat(node.has("subsumed-by")).isTrue();
        }
    }

    @Test
    public void testSingleIssue() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        assertValidJsonIssue(os.toString(), 1);
    }

    @Test
    public void testCodeField() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        String jsonText = os.toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonText);
        String code = rootNode.get("issues").get(0).get("code").asText();
        assertThat(code).isEqualTo("[scratchblocks]" + System.lineSeparator() +
                "repeat until <(x position) = (50):: #ff0000> // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator());
    }

    @Test
    public void testMultipleIssues() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/positionEqualsCheck.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        assertValidJsonIssue(os.toString(), 4);
    }

    @Test
    public void testFileOutput() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        Path tmpFile = Files.createTempFile(null, null);
        JSONReportGenerator generator = new JSONReportGenerator(tmpFile.toString());
        generator.generateReport(program, issues);

        String result = Files.readString(tmpFile);
        assertValidJsonIssue(result, 1);
        Files.delete(tmpFile);
    }

    @Test
    public void testReportWithLooseComment() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/emptySprite.json");

        EmptySprite emptySprite = new EmptySprite();
        Assertions.assertEquals(emptySprite.NAME, emptySprite.getName());
        Set<Issue> issues = emptySprite.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        assertValidJsonIssue(os.toString(), 1);
    }

    @Test
    public void testReportWithDuplicateInformation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/multiple_missingbroadcast.json");

        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        String issueText = os.toString();
        assertValidJsonIssue(issueText, 3);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(issueText);
        JsonNode issueNode = rootNode.get("issues");

        JsonNode issue0 = issueNode.get(0);
        JsonNode issue1 = issueNode.get(1);
        JsonNode issue2 = issueNode.get(2);
        Set<Integer> ids = new LinkedHashSet<>();
        ids.add(issue0.get("id").asInt());
        ids.add(issue1.get("id").asInt());
        ids.add(issue2.get("id").asInt());

        JsonNode duplicateList = issue0.get("duplicate-of");
        Set<Integer> duplicates = new LinkedHashSet<>(ids);
        duplicates.remove(issue0.get("id").intValue());
        assertThat(duplicateList.get(0).asInt()).isIn(duplicates);
        assertThat(duplicateList.get(1).asInt()).isIn(duplicates);

        duplicateList = issue1.get("duplicate-of");
        duplicates = new LinkedHashSet<>(ids);
        duplicates.remove(issue1.get("id").intValue());
        assertThat(duplicateList.get(0).asInt()).isIn(duplicates);
        assertThat(duplicateList.get(1).asInt()).isIn(duplicates);

        duplicateList = issue2.get("duplicate-of");
        duplicates = new LinkedHashSet<>(ids);
        duplicates.remove(issue2.get("id").intValue());
        assertThat(duplicateList.get(0).asInt()).isIn(duplicates);
        assertThat(duplicateList.get(1).asInt()).isIn(duplicates);
    }

    @Test
    public void testReportWithoutDuplicateInformation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");

        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        String issueText = os.toString();
        assertValidJsonIssue(issueText, 3);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(issueText);
        JsonNode issueNode = rootNode.get("issues");

        JsonNode issue0 = issueNode.get(0);
        JsonNode issue1 = issueNode.get(1);
        JsonNode issue2 = issueNode.get(2);

        JsonNode duplicateList = issue0.get("duplicate-of");
        assertThat(duplicateList.size()).isEqualTo(0);

        duplicateList = issue1.get("duplicate-of");
        assertThat(duplicateList.size()).isEqualTo(0);

        duplicateList = issue2.get("duplicate-of");
        assertThat(duplicateList.size()).isEqualTo(0);
    }

    @Test
    public void testReportWithSubsumptionInformation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/varcomparison_subsumption.json");
        Set<Issue> issues = new LinkedHashSet<>();

        ComparingLiterals finder = new ComparingLiterals();
        issues.addAll(finder.check(program));

        VariableAsLiteral literalFinder = new VariableAsLiteral();
        issues.addAll(literalFinder.check(program));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        String issueText = os.toString();
        assertValidJsonIssue(issueText, 2);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(issueText);
        JsonNode issueNode = rootNode.get("issues");
        JsonNode issue0 = issueNode.get(0);
        JsonNode issue1 = issueNode.get(1);

        JsonNode subsumption0 = issue0.get("subsumed-by");
        JsonNode subsumption1 = issue1.get("subsumed-by");

        if (issue0.get("finder").asText().equals(finder.getName())) {
            assertThat(subsumption0.size()).isEqualTo(0);
            assertThat(subsumption1.get(0).asInt()).isEqualTo(issue0.get("id").asInt());
        } else {
            assertThat(subsumption1.size()).isEqualTo(0);
            assertThat(subsumption0.get(0).asInt()).isEqualTo(issue1.get("id").asInt());
        }
    }
}

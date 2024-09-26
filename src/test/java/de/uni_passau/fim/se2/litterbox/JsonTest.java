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
package de.uni_passau.fim.se2.litterbox;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.metric.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.analytics.extraction.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.NewParser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public interface JsonTest {
    boolean LOAD_GENERAL = PropertyLoader.getSystemBooleanProperty("issues.load_general");
    boolean LOAD_MBLOCK = PropertyLoader.getSystemBooleanProperty("issues.load_mblock");

    default Program getAST(String fileName) throws IOException, ParsingException {
        NewParser parser = new NewParser();
        return parser.parseFile(Path.of(fileName).toFile());
    }

    default ControlFlowGraph getCFG(String fileName) throws IOException, ParsingException {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(getAST(fileName));
        return visitor.getControlFlowGraph();
    }

    // TODO: This is a bit redundant wrt getAST (it is added for the tests that have a static test fixture)
    static Program parseProgram(String fileName) throws IOException, ParsingException {
        NewParser parser = new NewParser();
        return parser.parseFile(Path.of(fileName).toFile());
    }

    default Set<Issue> generateIssues(IssueFinder finder, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        return finder.check(prog);
    }

    /**
     * Checks that the given AST part does not contain any {@link UnspecifiedStmt} or {@link UnspecifiedExpression}.
     *
     * @param root The root node of the (sub-)tree.
     */
    default void assertNoUnspecifiedBlocks(final ASTNode root) {
        final var unknownExpr = NodeFilteringVisitor.getBlocks(root, UnspecifiedExpression.class);
        assertThat(unknownExpr).isEmpty();

        final var unknownStmt = NodeFilteringVisitor.getBlocks(root, UnspecifiedStmt.class);
        assertThat(unknownStmt).isEmpty();
    }

    default void assertThatFinderReports(int expectedIssues, IssueFinder finder, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        Set<Issue> reports = finder.check(prog);
        Assertions.assertEquals(expectedIssues, reports.size());
    }

    default void assertThatExtractionReports(List<String> expected, NameExtraction finder, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        List<String> reports = finder.extractNames(prog);
        Assertions.assertEquals(expected, reports);
    }

    default void assertThatMetricReports(double expectedIssues, MetricExtractor<Program> finder, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        Assertions.assertEquals(expectedIssues, finder.calculateMetric(prog));
    }

    default void assertThatMetricReportsWithin(double expectedIssues, double epsilon, MetricExtractor<Program> finder, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        assertThat(finder.calculateMetric(prog)).isWithin(epsilon).of(expectedIssues);
    }

    default void assertNumberActorDefinitions(int expectedActors, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        ActorDefinitionList list = prog.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(expectedActors);
    }

    default Set<Issue> runFinder(Program program, IssueFinder issueFinder, boolean ignoreLooseBlocks) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        issueFinder.setIgnoreLooseBlocks(ignoreLooseBlocks);
        issues.addAll(issueFinder.check(program));
        return issues;
    }

    default Set<Issue> runFinders(Program program, List<IssueFinder> issueFinders, boolean ignoreLooseBlocks) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder iF : issueFinders) {
            iF.setIgnoreLooseBlocks(ignoreLooseBlocks);
            issues.addAll(iF.check(program));
        }
        return issues;
    }
}

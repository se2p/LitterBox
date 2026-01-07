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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Set;

/**
 * Base test class for fix heuristic tests using ScratchBlocks text format.
 *
 * <p>Tests can use either:
 * <ul>
 *   <li>{@link #parseProgram(String)} to parse ScratchBlocks code</li>
 *   <li>{@link #assertFixFound(AbstractFixHeuristic, Program)} to verify a fix is detected</li>
 *   <li>{@link #assertNoFixFound(AbstractFixHeuristic, Program)} to verify no fix is detected</li>
 * </ul>
 */
public abstract class AbstractFixHeuristicTest implements JsonTest {

    private static final String EMPTY_PROJECT = "src/test/fixtures/emptyProject.json";
    private static final String DEFAULT_SPRITE = "Sprite1";

    /**
     * Parse ScratchBlocks code into a Program.
     *
     * @param scratchBlocksCode the ScratchBlocks text (e.g., "when green flag clicked\nmove (10) steps")
     * @return the parsed Program with the code added to Sprite1
     */
    protected Program parseProgram(String scratchBlocksCode) throws IOException, ParsingException {
        Program baseProject = getAST(EMPTY_PROJECT);
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program program = parser.extendProject(baseProject, DEFAULT_SPRITE, scratchBlocksCode);
        ParentVisitor.apply(program);
        return program;
    }

    /**
     * Parse ScratchBlocks code for a specific sprite.
     */
    protected Program parseProgram(String scratchBlocksCode, String spriteName) throws IOException, ParsingException {
        Program baseProject = getAST(EMPTY_PROJECT);
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program program = parser.extendProject(baseProject, spriteName, scratchBlocksCode);
        ParentVisitor.apply(program);
        return program;
    }

    /**
     * Parse ScratchBlocks code for multiple sprites.
     *
     * <p>Uses a base project with multiple sprites (raceConditionDifferentActors.json).
     *
     * @param sprite1Code code for Sprite1 (or null to skip)
     * @param sprite2Code code for Sprite2 (or null to skip)
     * @return the parsed Program with code added to both sprites
     */
    protected Program parseMultiSpriteProgram(String sprite1Code, String sprite2Code) throws IOException, ParsingException {
        // Use a project that already has multiple sprites
        Program program = getAST("src/test/fixtures/smells/raceConditionDifferentActors.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();

        if (sprite1Code != null && !sprite1Code.isBlank()) {
            program = parser.extendProject(program, "Sprite1", sprite1Code);
        }
        if (sprite2Code != null && !sprite2Code.isBlank()) {
            program = parser.extendProject(program, "Sprite2", sprite2Code);
        }

        ParentVisitor.apply(program);
        return program;
    }

    /**
     * Assert that the given fix heuristic reports at least one fix.
     */
    protected void assertFixFound(AbstractFixHeuristic finder, Program program) {
        Set<FixResult> fixes = finder.check(program);
        Assertions.assertFalse(fixes.isEmpty(),
                "Expected at least one fix to be found, but found none.");

        FixResult fixResult = fixes.iterator().next();

        // Verify the original issue is tracked
        Assertions.assertNotNull(fixResult.originalIssue(),
                "Fix result must have an original issue.");

        // Code location can be null if the block was deleted (which is a valid fix)
        ASTNode loc = fixResult.fixedNode();

        // Verify it matches the original bug location (if known and location exists)
        if (loc != null && finder.getBugLocationBlockId() != null
                && !finder.getBugLocationBlockId().startsWith("__")) {
            boolean matches = AstNodeUtil.hasBlockId(loc, finder.getBugLocationBlockId());
            Assertions.assertTrue(matches,
                    "Fix location does not match original bug location ID: " + finder.getBugLocationBlockId());
        }
        // If loc is null, the block was deleted - this is a valid fix scenario
    }

    /**
     * Assert that the given fix heuristic reports at least one fix.
     * (Backward-compatible overload for JSON string sources.)
     */
    protected void assertFixFound(AbstractFixHeuristic finder, String jsonSource) throws ParsingException {
        Program program = new Scratch3Parser().parseString("test", jsonSource);
        assertFixFound(finder, program);
    }

    /**
     * Assert that the given fix heuristic reports no fixes.
     */
    protected void assertNoFixFound(AbstractFixHeuristic finder, Program program) {
        Set<FixResult> fixes = finder.check(program);
        Assertions.assertTrue(fixes.isEmpty(),
                "Expected NO fix to be found, but found: " + fixes);
    }

    /**
     * Assert that the given fix heuristic reports no fixes.
     * (Backward-compatible overload for JSON string sources.)
     */
    protected void assertNoFixFound(AbstractFixHeuristic finder, String jsonSource) throws ParsingException {
        Program program = new Scratch3Parser().parseString("test", jsonSource);
        assertNoFixFound(finder, program);
    }

    /**
     * Find and return the block ID of the first node matching the given predicate.
     * Useful for dynamically finding block IDs in parsed programs.
     *
     * @param program   the program to search
     * @param nodeClass the class of node to find
     * @return the block ID of the first matching node, or null if not found
     */
    protected <T extends ASTNode> String findBlockId(Program program, Class<T> nodeClass) {
        return findBlockId(program, nodeClass, node -> true);
    }

    /**
     * Find and return the block ID of the first node matching the given predicate.
     */
    protected <T extends ASTNode> String findBlockId(Program program, Class<T> nodeClass,
                                                     java.util.function.Predicate<T> predicate) {
        for (ASTNode node : program.getActorDefinitionList().getDefinitions()) {
            String id = findBlockIdRecursive(node, nodeClass, predicate);
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends ASTNode> String findBlockIdRecursive(ASTNode node, Class<T> nodeClass,
                                                            java.util.function.Predicate<T> predicate) {
        if (nodeClass.isInstance(node) && predicate.test((T) node)) {
            return AstNodeUtil.getBlockId(node);
        }
        for (ASTNode child : node.getChildren()) {
            String id = findBlockIdRecursive(child, nodeClass, predicate);
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    /**
     * Creates a minimal IssueDTO for testing purposes.
     *
     * @param blockId    the block ID
     * @param finderName the finder name
     * @return a minimal IssueDTO
     */
    protected static IssueDTO createTestIssueDTO(String blockId, String finderName) {
        return new IssueDTO(
                0, finderName, finderName, IssueType.BUG, 0, "Sprite1",
                blockId, null, null, null, null, null, null, 0, null, null
        );
    }

    /**
     * Creates a minimal IssueDTO for testing with just a block ID.
     */
    protected static IssueDTO createTestIssueDTO(String blockId) {
        return createTestIssueDTO(blockId, "TestFinder");
    }
}

/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Div;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Mult;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPERAND1_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPERAND2_KEY;

public class AstNodeUtil {
    private AstNodeUtil() {
        throw new IllegalCallerException("utility class");
    }

    /**
     * Gets the block id of the given node, if it has one.
     *
     * @param node Some node of the AST.
     * @return The block id of the node if it has one, {@code null} otherwise.
     */
    public static String getBlockId(final ASTNode node) {
        if (node.getMetadata() instanceof DataBlockMetadata block) {
            return block.getBlockId();
        } else if (node.getMetadata() instanceof NonDataBlockMetadata block) {
            return block.getBlockId();
        } else if (node.getMetadata() instanceof ProcedureMetadata procedure) {
            return ((NonDataBlockMetadata) procedure.getDefinition()).getBlockId();
        } else {
            return null;
        }
    }

    public static boolean isMetadata(final ASTNode node) {
        return node instanceof Metadata
                || node instanceof CommentMetadataList
                || node instanceof ImageMetadataList
                || node instanceof MonitorMetadataList
                || node instanceof SoundMetadataList;
    }

    /**
     * Gets all actors in the program except for the ones that have a default name.
     *
     * <p>If a sprite is ‘default’ is only determined by its name
     * (see {@link NodeNameUtil#hasDefaultName(ActorDefinition)}).
     *
     * @param program      Some program.
     * @param includeStage True, if the stage should be included as an actor.
     * @return The actors in the given program.
     */
    public static Stream<ActorDefinition> getActorsWithoutDefaultSprites(final Program program, boolean includeStage) {
        return getActors(program, includeStage).filter(Predicate.not(NodeNameUtil::hasDefaultName));
    }

    public static Stream<ActorDefinition> getActors(final Program program, boolean includeStage) {
        return program
                .getActorDefinitionList()
                .getDefinitions()
                .stream()
                .filter(actor -> includeStage || actor.isSprite());
    }

    /**
     * Finds the actor the given node belongs to.
     *
     * @param node Some {@link ASTNode}.
     * @return The actor the node belongs to, empty if the node belongs to no actor.
     */
    public static Optional<ActorDefinition> findActor(final ASTNode node) {
        return Optional.ofNullable(findParent(node, ActorDefinition.class));
    }

    /**
     * Finds a transitive parent of node of the requested type.
     *
     * @param node       Some node in the AST.
     * @param parentType The class the parent is represented by.
     * @return The parent in the AST of the requested type.
     *         Might return {@code node} itself if it has matching type.
     *         Returns {@code null} if no parent of the requested type could be found.
     */
    public static <T extends ASTNode> T findParent(final ASTNode node, final Class<T> parentType) {
        ASTNode currentNode = node;

        while (currentNode != null) {
            if (parentType.isAssignableFrom(currentNode.getClass())) {
                return parentType.cast(currentNode);
            }
            currentNode = currentNode.getParentNode();
        }

        return null;
    }

    /**
     * Tells if the given AST node is an input of the requested kind.
     * @param child     The node to inspect.
     * @param inputKind The request input kind (a string, corresponds to the input keys in the {@code project.json}.)
     * @return {@code true} if the {@code child} is used as the specified input, {@code false} otherwise.
     */
    public static boolean isInputOfKind(final ASTNode child, final String inputKind) {
        final IsInputOfVisitor visitor = new IsInputOfVisitor();
        child.getParentNode().accept(visitor);
        return visitor.inputs.containsKey(inputKind) && visitor.inputs.get(inputKind) == child;
    }

    /**
     * Replaces all parameter placeholders with the given substitution.
     *
     * <p>Replaces
     * <ul>
     *     <li>string parameters ({@code %s})</li>
     *     <li>boolean parameters ({@code %b})</li>
     *     <li>numeric parameters ({@code %n})</li>
     * </ul>
     *
     * @param procedureName The name of the procedure including the parameter placeholders.
     * @param replacement   The substitution string.
     * @return The procedure name with replaced parameter placeholders.
     */
    public static String replaceProcedureParams(final String procedureName, final String replacement) {
        return replaceProcedureParams(procedureName, replacement, replacement, replacement);
    }

    /**
     * Replaces all parameter placeholders with the given substitution.
     *
     * <p>Replaces
     * <ul>
     *     <li>string parameters ({@code %s})</li>
     *     <li>boolean parameters ({@code %b})</li>
     *     <li>numeric parameters ({@code %n})</li>
     * </ul>
     *
     * @param procedureName The name of the procedure including the parameter placeholders.
     * @param replacementS  The substitution string for the string parameters.
     * @param replacementB  The substitution string for the boolean parameters.
     * @param replacementN  The substitution string for the numeric parameters.
     * @return The procedure name with replaced parameter placeholders.
     */
    public static String replaceProcedureParams(
            final String procedureName, final String replacementS, final String replacementB, final String replacementN
    ) {
        return procedureName.replace("%s", replacementS)
                .replace("%b", replacementB)
                .replace("%n", replacementN)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static class IsInputOfVisitor implements ScratchVisitor, MusicExtensionVisitor, PenExtensionVisitor,
            TextToSpeechExtensionVisitor, TranslateExtensionVisitor {
        private final Map<String, ASTNode> inputs = new HashMap<>();

        private IsInputOfVisitor() {
        }

        @Override
        public void visit(Add node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(Minus node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(Mult node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(Div node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        // TODO: Which other methods from ScratchVisitor must be overridden?

        // TODO: Create overrides for methods from:
        //  - MusicExtensionVisitor?
        //  - PenExtensionVisitor?
        //  - TextToSpeechExtensionVisitor?
        //  - TranslateExtensionVisitor?
    }
}

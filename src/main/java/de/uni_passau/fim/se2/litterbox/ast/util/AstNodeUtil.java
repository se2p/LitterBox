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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteAllOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

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
     * Gets all scripts in the program.
     *
     * @param program Some program.
     * @return The scripts in the given program.
     */
    public static Stream<Script> getScripts(final Program program) {
        return program.getActorDefinitionList().getDefinitions().stream()
                .flatMap(actor -> actor.getScripts().getScriptList().stream());
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
     * Finds the actor the given name belongs to.
     *
     * @param name name of the searched actor.
     * @return The actor with the name, null if there is no actor with this name.
     */
    public static ActorDefinition findActorByName(Program program, final String name) {
        List<ActorDefinition> actors = program.getActorDefinitionList().getDefinitions();
        for (ActorDefinition actor : actors) {
            if (actor.getIdent().getName().equals(name)) {
                return actor;
            }
        }
        return null;
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

    /**
     * Creates a map that associates block IDs with the corresponding AST nodes, starting at the given root node.
     *
     * @param root The root of the tree.
     * @return A map from block IDs to their AST nodes in the tree with {@code root} as root node.
     */
    public static Map<String, ASTNode> getBlockMap(final ASTNode root) {
        final BlockMapVisitor visitor = new BlockMapVisitor();
        root.accept(visitor);
        return visitor.blockMap;
    }

    public static boolean hasBlockId(ASTNode node, String blockId) {
        return Objects.equals(getBlockId(node), blockId);
    }

    private static class BlockMapVisitor implements ScratchVisitor, MusicExtensionVisitor, PenExtensionVisitor,
            TextToSpeechExtensionVisitor, TranslateExtensionVisitor {
        private final Map<String, ASTNode> blockMap = new LinkedHashMap<>();

        private BlockMapVisitor() {
        }

        @Override
        public void visit(final ASTNode node) {
            final String blockId = AstNodeUtil.getBlockId(node);
            if (blockId != null) {
                blockMap.put(blockId, node);
            }
            visitChildren(node);
        }
    }

    private static ASTNode getFirstStmtOrNull(final StmtList stmtList) {
        return stmtList.hasStatements() ? stmtList.getStatement(0) : null;
    }

    private static ASTNode getFirstStmtSubstack(final ASTNode node) {
        if (node instanceof IfStmt ifStmt) {
            return getFirstStmtOrNull(ifStmt.getThenStmts());
        }

        if (node instanceof LoopStmt loopStmt) {
            return getFirstStmtOrNull(loopStmt.getStmtList());
        }

        return null;
    }

    private static ASTNode getFirstStmtSubstack2(final ASTNode node) {
        if (node instanceof IfElseStmt ifElseStmt) {
            return getFirstStmtOrNull(ifElseStmt.getElseStmts());
        }

        return null;
    }

    /**
     * Tells if the given AST node is an input of the requested kind.
     *
     * @param child     The node to inspect.
     * @param inputKind The request input kind (a string, corresponds to the input keys in the {@code project.json}.)
     * @return {@code true} if the {@code child} is used as the specified input, {@code false} otherwise.
     */
    public static boolean isInputOfKind(final ASTNode child, final String inputKind) {
        ASTNode parent = child.getParentNode();

        if (parent == null) {
            return false;
        }

        if (parent instanceof StmtList) {
            parent = parent.getParentNode();
        }

        final IsInputOfVisitor visitor = new IsInputOfVisitor();
        parent.accept(visitor);

        return visitor.inputs.containsKey(inputKind) && visitor.inputs.get(inputKind) == child;
    }

    private static class IsInputOfVisitor implements ScratchVisitor, PenExtensionVisitor {
        private final Map<String, ASTNode> inputs = new LinkedHashMap<>();

        private IsInputOfVisitor() {
        }

        /*
         * Control blocks
         */

        @Override
        public void visit(final RepeatForeverStmt node) {
            inputs.put(SUBSTACK_KEY, getFirstStmtSubstack(node));
        }

        @Override
        public void visit(final IfThenStmt node) {
            inputs.put(CONDITION_KEY, node.getBoolExpr());
            inputs.put(SUBSTACK_KEY, getFirstStmtSubstack(node));
        }

        @Override
        public void visit(final IfElseStmt node) {
            inputs.put(CONDITION_KEY, node.getBoolExpr());
            inputs.put(SUBSTACK_KEY, getFirstStmtSubstack(node));
            inputs.put(SUBSTACK2_KEY, getFirstStmtSubstack2(node));
        }

        @Override
        public void visit(final RepeatTimesStmt node) {
            inputs.put(TIMES_KEY, node.getTimes());
            inputs.put(SUBSTACK_KEY, getFirstStmtSubstack(node));
        }

        @Override
        public void visit(final UntilStmt node) {
            inputs.put(CONDITION_KEY, node.getBoolExpr());
            inputs.put(SUBSTACK_KEY, getFirstStmtSubstack(node));
        }

        @Override
        public void visit(final WaitSeconds node) {
            inputs.put(DURATION_KEY, node.getSeconds());
        }

        @Override
        public void visit(final WaitUntil node) {
            inputs.put(CONDITION_KEY, node.getUntil());
        }

        @Override
        public void visit(final CreateCloneOf node) {
            inputs.put(CLONE_OPTION, node.getCloneChoice());
        }

        /*
         * Event blocks
         */

        @Override
        public void visit(final BroadcastAndWait node) {
            inputs.put(BROADCAST_INPUT_KEY, node.getMessage());
        }

        @Override
        public void visit(final Broadcast node) {
            inputs.put(BROADCAST_INPUT_KEY, node.getMessage());
        }

        @Override
        public void visit(final AttributeAboveValue node) {
            inputs.put(VALUE_KEY, node.getValue());
        }

        /*
         * Looks blocks
         */

        @Override
        public void visit(final ChangeGraphicEffectBy node) {
            inputs.put(CHANGE_KEY, node.getValue());
        }

        @Override
        public void visit(final ChangeLayerBy node) {
            inputs.put(NUM_KEY, node.getNum());
        }

        @Override
        public void visit(final ChangeSizeBy node) {
            inputs.put(CHANGE_KEY, node.getNum());
        }

        @Override
        public void visit(final Say node) {
            inputs.put(MESSAGE_KEY, node.getString());
        }

        @Override
        public void visit(final SayForSecs node) {
            inputs.put(MESSAGE_KEY, node.getString());
            inputs.put(SECS_KEY, node.getSecs());
        }

        @Override
        public void visit(final SetGraphicEffectTo node) {
            inputs.put(VALUE_KEY, node.getValue());
        }

        @Override
        public void visit(final SetSizeTo node) {
            inputs.put(SIZE_KEY_CAP, node.getPercent());
        }

        @Override
        public void visit(final SwitchBackdropAndWait node) {
            inputs.put(BACKDROP_INPUT, node.getElementChoice());
        }

        @Override
        public void visit(final SwitchBackdrop node) {
            inputs.put(BACKDROP_INPUT, node.getElementChoice());
        }

        @Override
        public void visit(final SwitchCostumeTo node) {
            inputs.put(COSTUME_INPUT, node.getCostumeChoice());
        }

        @Override
        public void visit(final Think node) {
            inputs.put(MESSAGE_KEY, node.getThought());
        }

        @Override
        public void visit(final ThinkForSecs node) {
            inputs.put(MESSAGE_KEY, node.getThought());
            inputs.put(SECS_KEY, node.getSecs());
        }

        /*
         * Motion blocks
         */

        @Override
        public void visit(final SetYTo node) {
            inputs.put(Y, node.getNum());
        }

        @Override
        public void visit(final SetXTo node) {
            inputs.put(X, node.getNum());
        }

        @Override
        public void visit(final PointTowards node) {
            inputs.put(TOWARDS_KEY, node.getPosition());
        }

        @Override
        public void visit(final ChangeYBy node) {
            inputs.put(DY_KEY, node.getNum());
        }

        @Override
        public void visit(final ChangeXBy node) {
            inputs.put(DX_KEY, node.getNum());
        }

        @Override
        public void visit(final PointInDirection node) {
            inputs.put(DIRECTION_KEY_CAP, node.getDirection());
        }

        @Override
        public void visit(final GlideSecsToXY node) {
            inputs.put(X, node.getX());
            inputs.put(Y, node.getY());
            inputs.put(SECS_KEY, node.getSecs());
        }

        @Override
        public void visit(final GlideSecsTo node) {
            inputs.put(TO_KEY, node.getPosition());
            inputs.put(SECS_KEY, node.getSecs());
        }

        @Override
        public void visit(final GoToPosXY node) {
            inputs.put(X, node.getX());
            inputs.put(Y, node.getY());
        }

        @Override
        public void visit(final GoToPos node) {
            inputs.put(TO_KEY, node.getPosition());
        }

        @Override
        public void visit(final TurnRight node) {
            inputs.put(DEGREES_KEY, node.getDegrees());
        }

        @Override
        public void visit(final TurnLeft node) {
            inputs.put(DEGREES_KEY, node.getDegrees());
        }

        @Override
        public void visit(final MoveSteps node) {
            inputs.put(STEPS_KEY, node.getSteps());
        }

        /*
         * Operator blocks
         */

        @Override
        public void visit(final Add node) {
            inputs.put(NUM1_KEY, node.getOperand1());
            inputs.put(NUM2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Minus node) {
            inputs.put(NUM1_KEY, node.getOperand1());
            inputs.put(NUM2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Mult node) {
            inputs.put(NUM1_KEY, node.getOperand1());
            inputs.put(NUM2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Div node) {
            inputs.put(NUM1_KEY, node.getOperand1());
            inputs.put(NUM2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Mod node) {
            inputs.put(NUM1_KEY, node.getOperand1());
            inputs.put(NUM2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Equals node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final LessThan node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final BiggerThan node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final And node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Or node) {
            inputs.put(OPERAND1_KEY, node.getOperand1());
            inputs.put(OPERAND2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Not node) {
            inputs.put(OPERAND_KEY, node.getOperand1());
        }

        @Override
        public void visit(final LetterOf node) {
            inputs.put(LETTER_KEY, node.getNum());
            inputs.put(STRING_KEY, node.getStringExpr());
        }

        @Override
        public void visit(final Join node) {
            inputs.put(STRING1_KEY, node.getOperand1());
            inputs.put(STRING2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final NumFunctOf node) {
            inputs.put(NUM_KEY, node.getOperand2());
        }

        @Override
        public void visit(final Round node) {
            inputs.put(NUM_KEY, node.getOperand1());
        }

        @Override
        public void visit(final LengthOfString node) {
            inputs.put(STRING_KEY, node.getStringExpr());
        }

        @Override
        public void visit(final StringContains node) {
            inputs.put(STRING1_KEY, node.getContaining());
            inputs.put(STRING2_KEY, node.getContained());
        }

        @Override
        public void visit(final PickRandom node) {
            inputs.put(FROM_KEY, node.getOperand1());
            inputs.put(TO_KEY, node.getOperand2());
        }

        /*
         * Sensing blocks
         */

        @Override
        public void visit(final KeyPressed node) {
            inputs.put(KEY_OPTION, node.getKey());
        }

        @Override
        public void visit(final AttributeOf node) {
            inputs.put(OBJECT_KEY, node.getElementChoice());
        }

        @Override
        public void visit(final AskAndWait node) {
            inputs.put(QUESTION_KEY, node.getQuestion());
        }

        @Override
        public void visit(final DistanceTo node) {
            inputs.put(DISTANCETOMENU_KEY, node.getPosition());
        }

        @Override
        public void visit(final ColorTouchingColor node) {
            inputs.put(COLOR_KEY, node.getOperand1());
            inputs.put(COLOR2_KEY, node.getOperand2());
        }

        @Override
        public void visit(final SpriteTouchingColor node) {
            inputs.put(COLOR_KEY, node.getColor());
        }

        @Override
        public void visit(final Touching node) {
            inputs.put(TOUCHINGOBJECTMENU, node.getTouchable());
        }

        /*
         * Sound blocks
         */

        @Override
        public void visit(final SetVolumeTo node) {
            inputs.put(VOLUME_KEY_CAPS, node.getVolumeValue());
        }

        @Override
        public void visit(final ChangeVolumeBy node) {
            inputs.put(VOLUME_KEY_CAPS, node.getVolumeValue());
        }

        @Override
        public void visit(final SetSoundEffectTo node) {
            inputs.put(VALUE_KEY, node.getValue());
        }

        @Override
        public void visit(final ChangeSoundEffectBy node) {
            inputs.put(VALUE_KEY, node.getValue());
        }

        @Override
        public void visit(final PlaySoundUntilDone node) {
            inputs.put(SOUND_MENU, node.getElementChoice());
        }

        @Override
        public void visit(final StartSound node) {
            inputs.put(SOUND_MENU, node.getElementChoice());
        }

        /*
         * Variable blocks
         */

        @Override
        public void visit(final ListContains node) {
            inputs.put(ITEM_KEY, node.getElement());
        }

        @Override
        public void visit(final ItemOfVariable node) {
            inputs.put(INDEX_KEY, node.getNum());
        }

        @Override
        public void visit(final IndexOf node) {
            inputs.put(ITEM_KEY, node.getExpr());
        }

        @Override
        public void visit(final ReplaceItem node) {
            inputs.put(ITEM_KEY, node.getString());
            inputs.put(INDEX_KEY, node.getIndex());
        }

        @Override
        public void visit(final InsertAt node) {
            inputs.put(ITEM_KEY, node.getString());
            inputs.put(INDEX_KEY, node.getIndex());
        }

        @Override
        public void visit(final DeleteAllOf node) {
            inputs.put(INDEX_KEY, node.getIdentifier());
        }

        @Override
        public void visit(final AddTo node) {
            inputs.put(ITEM_KEY, node.getString());
        }

        @Override
        public void visit(final ChangeVariableBy node) {
            inputs.put(VALUE_KEY, node.getExpr());
        }

        @Override
        public void visit(final SetVariableTo node) {
            inputs.put(VALUE_KEY, node.getExpr());
        }

        /*
         * Pen blocks
         */

        @Override
        public void visit(final SetPenSizeTo node) {
            inputs.put(SIZE_KEY_CAP, node.getValue());
        }

        @Override
        public void visit(final ChangePenSizeBy node) {
            inputs.put(SIZE_KEY_CAP, node.getValue());
        }

        @Override
        public void visit(final SetPenColorParamTo node) {
            inputs.put(COLOR_PARAM_BIG_KEY, node.getValue());
        }

        @Override
        public void visit(final ChangePenColorParamBy node) {
            inputs.put(COLOR_PARAM_BIG_KEY, node.getParam());
            inputs.put(VALUE_KEY, node.getValue());
        }

        @Override
        public void visit(final SetPenColorToColorStmt node) {
            inputs.put(COLOR_KEY, node.getColorExpr());
        }
    }
}

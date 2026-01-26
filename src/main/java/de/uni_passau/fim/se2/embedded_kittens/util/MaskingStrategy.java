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
package de.uni_passau.fim.se2.embedded_kittens.util;

import de.uni_passau.fim.se2.embedded_kittens.tokenizer.Token;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * A masking strategy governs which entities of a Scratch program will be represented by a special mask token when
 * tokenizing the program. Several strategies are available. This class provides static factory methods for them. To
 * disable masking, use {@link MaskingStrategy#none()}.
 *
 * @see Token#MASK
 */
public abstract sealed class MaskingStrategy {

    /**
     * Tells whether to insert a mask token for the given AST node, according to the current masking strategy.
     *
     * @param node The node to tokenize.
     * @return {@code true} if the node should be masked, {@code false} otherwise.
     */
    public abstract boolean shouldBeMasked(ASTNode node);

    /**
     * Do not mask.
     *
     * @return Strategy that does not mask.
     */
    public static MaskingStrategy.None none() {
        return None.getInstance();
    }

    /**
     * Mask the Scratch block with the given {@code blockId}.
     *
     * @param blockId The ID of the block to mask.
     * @return Strategy that masks the specified block.
     */
    public static MaskingStrategy.Block block(final String blockId) {
        Preconditions.checkNotNull(blockId);
        return new Block(blockId);
    }

    /**
     * Mask the input expression of a block. The input is identified by the {@code blockId} of the parent (the block
     * that takes the input) and the {@code inputKey} of the input. Input keys correspond to the keys used in the
     * {@code input} section of the Scratch JSON. They keys {@code SUBSTACK} and {@code SUBSTACK2} are not supported.
     *
     * @param blockId  The ID of the block that takes the input.
     * @param inputKey The key of the input.
     * @return Strategy that masks the specified input.
     */
    public static MaskingStrategy.Input input(final String blockId, final String inputKey) {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(inputKey);
        return new Input(blockId, inputKey);
    }

    /**
     * Mask the fixed node option (aka the rectangular dropdown menu) of the block with the given {@code blockId}.
     *
     * @param blockId The id of the block that offers the fixed node option.
     * @return Strategy that masks the specified fixed node option.
     */
    public static MaskingStrategy.FixedOption fixedOption(final String blockId) {
        Preconditions.checkNotNull(blockId);
        return new FixedOption(blockId);
    }

    /**
     * A strategy that does not mask.
     */
    public static final class None extends MaskingStrategy {

        private static None INSTANCE;

        private None() {
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            return false;
        }

        private static None getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new None();
            }

            return INSTANCE;
        }
    }

    /**
     * A strategy that masks statement or expression blocks. In general, this strategy can be used to mask any entity of
     * a Scratch program that can be identified by a single block ID.
     */
    public static final class Block extends MaskingStrategy {

        private final String blockId;

        private Block(final String blockId) {
            this.blockId = blockId;
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            return blockId.equals(AstNodeUtil.getBlockId(node));
        }
    }

    /**
     * A strategy that masks expression inputs, i.e., string/number reporters, boolean reporters, and primitives.
     *
     * <p>Expressions that have their own block ID (such as {@code operator_and} and {@code sensing_current}) can also
     * be masked by the block masking strategy. However, masking primitive inputs (e.g., the literal "1") is only
     * possible via the input masking strategy.
     *
     * <p>While technically also being inputs, this strategy does not support the inputs {@code SUBSTACK} or {@code
     * SUBSTACK2}, because they refer to entire statement lists, not single expressions.
     */
    public static final class Input extends MaskingStrategy {

        private final String blockId;
        private final String inputKey;

        private Input(final String blockId, final String inputKey) {
            this.blockId = blockId;
            this.inputKey = inputKey;
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            return blockId.equals(AstNodeUtil.getBlockId(node.getParentNode()))
                    && AstNodeUtil.isInputOfKind(node, inputKey);
        }
    }

    /**
     * A strategy that masks the fixed node option (rectangular dropdown menu) of a block.
     */
    public static final class FixedOption extends MaskingStrategy {

        private final String blockId;

        private FixedOption(final String blockId) {
            this.blockId = blockId;
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            final boolean isStopBlock = node instanceof StopAll
                    || node instanceof StopThisScript
                    || node instanceof StopOtherScriptsInSprite;

            // Special handling required for stop blocks due to inconsistent representation in the LitterBox AST.
            return isStopBlock
                    ? blockId.equals(AstNodeUtil.getBlockId(node))
                    : blockId.equals(AstNodeUtil.getBlockId(node.getParentNode()));
        }
    }
}

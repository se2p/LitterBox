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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.AstnnAstNode;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.NodeType;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.StatementType;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AbstractTokenCheck;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class AstnnAbstractTokenTest extends AbstractTokenCheck {

    @Override
    protected Set<String> getSpecialAllowedTokens() {
        final Set<String> allowedTokens = new HashSet<>();

        allowedTokens.add("block");
        allowedTokens.add("if");

        final List<Class<? extends Enum<?>>> enums = List.of(NodeType.class, StatementType.class);
        for (final var e : enums) {
            addEnumValuesToSet(allowedTokens, e);
        }

        return allowedTokens;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/customBlocks.json",
            "src/test/fixtures/ml_preprocessing/astnn/custom_block.json",
            "src/test/fixtures/ml_preprocessing/astnn/messages.json",
            "src/test/fixtures/ml_preprocessing/shared/music_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/pen_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/tts_blocks.json",
    })
    void testAllBlocksVisitableAbstract(final String filename) throws Exception {
        final Program program = getAST(filename);
        final AstnnNode node = ToAstnnTransformer.transform(program, true, true, true);
        assertThat(node).isNotNull();
        assertThat(node).isInstanceOf(AstnnAstNode.class);

        // do not check root node, as that is the program name
        for (final var child : node.children()) {
            checkContainsOnlyAllowedLabels(child);
        }
    }

    private void checkContainsOnlyAllowedLabels(final AstnnNode node) {
        checkNodeLabel(node.label());

        for (final var child : node.children()) {
            checkContainsOnlyAllowedLabels(child);
        }
    }
}

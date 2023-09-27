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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class AstnnNodeTest {
    @Test
    void emptyLabelLeaf() {
        final AstnnNode node = AstnnAstNodeFactory.build("");
        assertThat(node.label()).isEqualTo(NodeType.EMPTY_STRING.name());
    }

    @Test
    void emptyLabelNode() {
        final AstnnNode child = AstnnAstNodeFactory.build("child");
        final AstnnNode node = AstnnAstNodeFactory.build("", List.of(child));
        assertThat(node.label()).isEqualTo(NodeType.EMPTY_NAME.name());
    }

    @Test
    void blankLabelLeaf() {
        final AstnnNode node = AstnnAstNodeFactory.build("  \t ");
        assertThat(node.label()).isEqualTo(" ");
    }

    @Test
    void blankLabelNode() {
        // custom blocks or parameters with whitespace-only names get the same label
        final AstnnNode child = AstnnAstNodeFactory.build("child");
        final AstnnNode node = AstnnAstNodeFactory.build("  \t\n  ", List.of(child));
        assertThat(node.label()).isEqualTo(NodeType.EMPTY_NAME.name());
    }

    @Test
    void leafDepth() {
        final AstnnNode leaf = AstnnAstNodeFactory.build("abc");
        assertThat(leaf.getTreeDepth()).isEqualTo(1);
    }

    @Test
    void nodeDepth() {
        final AstnnNode leaf1 = AstnnAstNodeFactory.build("a");
        final AstnnNode leaf2 = AstnnAstNodeFactory.build("b");
        final AstnnNode node1 = AstnnAstNodeFactory.build("n1", List.of(leaf1, leaf2));

        final AstnnNode leaf3 = AstnnAstNodeFactory.build("c");
        final AstnnNode node2 = AstnnAstNodeFactory.build("n2", List.of(node1, leaf3));

        assertThat(node2.getTreeDepth()).isEqualTo(3);
    }
}

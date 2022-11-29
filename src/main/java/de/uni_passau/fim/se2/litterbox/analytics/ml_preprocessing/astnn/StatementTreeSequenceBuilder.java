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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.AstnnAstNodeFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.NodeType;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.StatementTreeSequence;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class StatementTreeSequenceBuilder {
    private StatementTreeSequenceBuilder() {
        throw new IllegalCallerException("utility class");
    }

    public static StatementTreeSequence build(
            final Program program, boolean includeStage, boolean includeDefaultSprites, boolean abstractTokens
    ) {
        final AstnnNode node = ToAstnnTransformer.transform(
                program, includeStage, includeDefaultSprites, abstractTokens
        );
        final List<AstnnNode> statementTrees = build(node);
        return new StatementTreeSequence(program.getIdent().getName(), statementTrees);
    }

    public static Stream<StatementTreeSequence> buildPerActor(
            final Program program, boolean includeStage, boolean includeDefaultSprites, boolean abstractTokens
    ) {
        final Stream<ActorDefinition> actors;
        if (includeDefaultSprites) {
            actors = AstNodeUtil.getActors(program, includeStage);
        } else {
            actors = AstNodeUtil.getActorsWithoutDefaultSprites(program, includeStage);
        }

        return actors.map(actor -> build(program, actor, abstractTokens));
    }

    /**
     * Builds the statement tree sequence for the given actor node.
     *
     * @param program The program the actor belongs to. Required to be able to resolve custom procedure names.
     * @param actor The actor for which the statement trees should be generated.
     * @param abstractTokens If literals and variable names should be represented by abstract tokens instead of values.
     * @return The statement tree sequence for the actor.
     */
    public static StatementTreeSequence build(
            final Program program, final ActorDefinition actor, boolean abstractTokens
    ) {
        final AstnnNode node = ToAstnnTransformer.transform(program, actor, abstractTokens);
        final List<AstnnNode> statementTrees = build(node);
        final String label = NodeNameUtil.normalizeSpriteName(actor).orElse(NodeType.EMPTY_STRING.toString());
        return new StatementTreeSequence(label, statementTrees);
    }

    private static List<AstnnNode> build(final AstnnNode rootNode) {
        final List<AstnnNode> list = new ArrayList<>();
        build(rootNode, list);
        return list;
    }

    private static void build(final AstnnNode node, final List<AstnnNode> sequence) {
        if (node.isStatement()) {
            sequence.add(node.asStatementTree());
        }
        for (AstnnNode c : node.children()) {
            build(c, sequence);
        }
        if (node.hasBlock()) {
            sequence.add(AstnnAstNodeFactory.blockEnd());
        }
    }
}

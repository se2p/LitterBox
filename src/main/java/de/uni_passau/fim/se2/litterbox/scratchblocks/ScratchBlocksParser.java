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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContext;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A parser of ScratchBlocks code into a LitterBox AST.
 *
 * <p>The parser regularly checks for {@link Thread#interrupted()} to abort the parsing early. However, this will not
 * immediately abort the parsing. Even though it is checked regularly inside the parsing state machine to achieve a
 * best-effort cancellation, there may be situations where the parser spends a long time in deeper internal states in
 * which the marker is only rarely checked.
 * Interrupted parsers throw a {@link ParseCancellationException}.
 *
 * @see <a href="https://en.scratch-wiki.info/wiki/Block_Plugin">ScratchBlocks syntax</a>
 */
public class ScratchBlocksParser {

    // todo: probably similar methods for whole actors and programs?
    public ScriptEntity parseScript(final String scratchBlocksCode) {
        return parseScript(scratchBlocksCode, new AtomicBoolean(false));
    }

    public ScriptList parseScriptList(final String scratchBlocksCode) {
        ParentVisitor visitor = new ParentVisitor();
        if (scratchBlocksCode.isBlank()) {
            Script script = new Script(new Never(), new StmtList());
            script.accept(visitor);
            List<Script> scripts = new ArrayList<>();
            scripts.add(script);
            ScriptList scriptList = new ScriptList(scripts);
            return scriptList;
        }

        final de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksParser parser = buildParser(
                new AtomicBoolean(false), scratchBlocksCode
        );
        final ParseTree tree = parser.scriptList();

        final ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        final ASTNode node = vis.visit(tree);

        if (node instanceof ScriptList scriptList) {
            scriptList.accept(visitor);
            return scriptList;
        } else if (node == null) {
            return null;
        } else {
            throw new IllegalArgumentException(
                    "Could not parse ScratchBlocks code as script. Got: " + node.getClass().getSimpleName()
            );
        }
    }

    /**
     * Parses the given script in ScratchBlocks code into an AST.
     *
     * <p>The {@code cancelMarker} is checked in addition to {@link Thread#interrupted()}. The same limitations as
     * described in {@link ScratchBlocksParser} apply.
     *
     * @param scratchBlocksCode Some ScratchBlocks code.
     * @param cancelMarker      Will try to interrupt the parsing when this marker gets set to {@code true}.
     * @return The parsed script or custom procedure definition.
     */
    public ScriptEntity parseScript(final String scratchBlocksCode, final AtomicBoolean cancelMarker) {
        ParentVisitor visitor = new ParentVisitor();
        if (scratchBlocksCode.isBlank()) {
            Script script = new Script(new Never(), new StmtList());
            script.accept(visitor);
            return script;
        }

        final de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksParser parser = buildParser(
                cancelMarker, scratchBlocksCode
        );
        final ParseTree tree = parser.script();

        final ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        final ASTNode node = vis.visit(tree);

        if (node instanceof ScriptEntity script) {
            script.accept(visitor);
            return script;
        } else if (node == null) {
            return null;
        } else {
            throw new IllegalArgumentException(
                    "Could not parse ScratchBlocks code as script. Got: " + node.getClass().getSimpleName()
            );
        }
    }

    private de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksParser buildParser(
            final AtomicBoolean cancelMarker, final String scratchBlocks
    ) {
        final ScratchBlocksLexer lexer = new ScratchBlocksLexer(CharStreams.fromString(scratchBlocks));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ScratchBlocksInterruptibleParser(tokens, cancelMarker);
    }

    private static final class ScratchBlocksInterruptibleParser
            extends de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksParser {

        public ScratchBlocksInterruptibleParser(final TokenStream input, final AtomicBoolean cancelMarker) {
            super(input);
            _interp = new InterruptibleParserATNSimulator(
                    this, _ATN, _decisionToDFA, _sharedContextCache, cancelMarker
            );
        }
    }

    private static final class InterruptibleParserATNSimulator extends ParserATNSimulator {

        private final AtomicBoolean cancelMarker;

        public InterruptibleParserATNSimulator(
                final Parser parser,
                final ATN atn,
                final DFA[] decisionToDFA,
                final PredictionContextCache sharedContextCache,
                final AtomicBoolean cancelMarker
        ) {
            super(parser, atn, decisionToDFA, sharedContextCache);
            this.cancelMarker = cancelMarker;
        }

        @Override
        public PredictionContext getCachedContext(final PredictionContext context) {
            if (cancelMarker.get() || Thread.interrupted()) {
                throw new ParseCancellationException("Parsing interrupted.");
            }

            return super.getCachedContext(context);
        }
    }
}

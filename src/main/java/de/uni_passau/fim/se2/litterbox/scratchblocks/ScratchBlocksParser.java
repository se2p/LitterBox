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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
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
import java.util.Collections;
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
    public Script parseScript(final String scratchBlocksCode) {
        return parseScript("Stage", scratchBlocksCode, new AtomicBoolean(false));
    }

    public Script parseScript(final String actorName, final String scratchBlocksCode) {
        return parseScript(actorName, scratchBlocksCode, new AtomicBoolean(false));
    }

    public ScriptEntity parseScriptEntity(final String actorName, final String scratchBlocksCode) {
        ActorContent content = parseActorContent(scratchBlocksCode, actorName);
        if (content != null) {
            if (content.scripts().getSize() > 0) {
                return content.scripts().getScript(0);
            } else if (!content.procedures().getList().isEmpty()) {
                return content.procedures().getList().getFirst();
            }
            throw new IllegalArgumentException(
                    "ActorContent has no scripts to add."
            );
        }
        throw new IllegalArgumentException(
                "Unknown actor '" + actorName + "has no scripts to add."
        );
    }

    public Program extendProject(Program baseProject, String actorName, String additionalCode) {
        if (AstNodeUtil.findActorByName(baseProject, actorName) == null) {
            throw new IllegalArgumentException(
                    "Unknown actor '" + actorName + "' in project '" + baseProject.getIdent().getName() + "'."
            );
        }

        CloneVisitor cloneVisitor = new CloneVisitor();
        Program extendedProject = (Program) baseProject.accept(cloneVisitor);
        ActorContent additionalContent = parseActorContent(additionalCode, actorName);
        ScratchProjectMerger merger = new ScratchProjectMerger();
        return merger.updateProject(extendedProject, actorName, additionalContent);
    }

    public ActorContent parseActorContent(final String scratchBlocksCode) {
        return parseActorContent(scratchBlocksCode, "Stage");
    }

    public ActorContent parseActorContent(final String scratchBlocksCode, String actorName) {
        ParentVisitor visitor = new ParentVisitor();
        if (scratchBlocksCode.isBlank()) {
            Script script = new Script(new Never(), new StmtList());
            script.accept(visitor);
            List<Script> scripts = new ArrayList<>();
            scripts.add(script);
            ScriptList scriptList = new ScriptList(scripts);
            return new ActorContent(scriptList, new ProcedureDefinitionList(Collections.emptyList()));
        }

        final de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksParser parser = buildParser(
                new AtomicBoolean(false), scratchBlocksCode
        );
        final ParseTree tree = parser.actorContent();

        final ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        vis.setCurrentActor(actorName);
        final ASTNode node = vis.visit(tree);

        if (node instanceof ActorContentHelperNode actorContentHelperNode) {
            return new ActorContent(actorContentHelperNode.scripts(), actorContentHelperNode.procedures());
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
    public Script parseScript(
            final String actorName, final String scratchBlocksCode, final AtomicBoolean cancelMarker
    ) {
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
        vis.setCurrentActor(actorName);
        final ASTNode node = vis.visit(tree);

        if (node instanceof Script script) {
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
        // without the final newline our parser does not work
        final ScratchBlocksLexer lexer = new ScratchBlocksLexer(CharStreams.fromString(scratchBlocks.trim() + "\n"));
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

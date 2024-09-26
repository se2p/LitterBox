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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.EventOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.*;
import java.util.logging.Logger;

final class RawScriptConverter {

    private final Logger log = Logger.getLogger(RawScriptConverter.class.getName());

    private final ProgramParserState state;
    private final RawStmtConverter stmtConverter;

    private RawScriptConverter(final ProgramParserState state) {
        this.state = state;
        this.stmtConverter = new RawStmtConverter(state);
    }

    static Script convertScript(final ProgramParserState state, final RawBlockId root) {
        final RawScriptConverter converter = new RawScriptConverter(state);
        return converter.convertScript(root);
    }

    private Script convertScript(final RawBlockId rootId) {
        final RawBlock rootBlock = state.getBlock(rootId);

        if (rootBlock instanceof RawBlock.RawRegularBlock regularBlock) {
            final Event event;
            final Optional<RawBlockId> stmtListStart;

            if (isEvent(regularBlock)) {
                event = EventConverter.convertEvent(state, rootId, regularBlock);
                stmtListStart = regularBlock.next();
            } else {
                event = new Never();
                stmtListStart = Optional.of(rootId);
            }

            final StmtList stmtList = stmtListStart.map(this::convertStmtList).orElseGet(StmtList::new);

            if (event instanceof Never && !stmtList.hasStatements()) {
                // no event and no statements, this cannot be a proper script
                return null;
            }

            return new Script(event, stmtList);
        } else {
            final ExpressionStmt expressionStmt = ExprConverter.convertExprStmt(state, rootId, rootBlock);
            return new Script(new Never(), new StmtList(expressionStmt));
        }
    }

    private static boolean isEvent(final RawBlock.RawRegularBlock block) {
        return EventOpcode.contains(block.opcode());
    }

    /**
     * Converts a statement list.
     *
     * @param state The current parser state.
     * @param root The first block of the statement list.
     * @return a statement list, or {@code null} in case the {@code root} does not point to a statement block.
     */
    static StmtList convertStmtList(final ProgramParserState state, final RawBlockId root) {
        final RawScriptConverter converter = new RawScriptConverter(state);
        return converter.convertStmtList(root);
    }

    private StmtList convertStmtList(final RawBlockId root) {
        final List<Stmt> stmts = new ArrayList<>();

        final RawBlock startingBlock = state.getBlock(root);

        if (startingBlock instanceof RawBlock.ArrayBlock) {
            final Stmt stmt = stmtConverter.convertStmt(root, startingBlock);
            stmts.add(stmt);
        } else {
            RawBlockId currentId = root;
            RawBlock currentBlock = startingBlock;

            while (currentId != null && currentBlock instanceof RawBlock.RawRegularBlock regularBlock) {
                final String opcode = regularBlock.opcode();
                if (isIgnoredBlockForStmtListConverter(opcode)) {
                    // don't parse these blocks here (since they are menus and procedures)
                    return null;
                } else if (isShadowParameter(regularBlock)) {
                    return null;
                } else {
                    try {
                        final Stmt stmt = stmtConverter.convertStmt(currentId, currentBlock);
                        stmts.add(stmt);
                    } catch (InternalParsingException | IllegalArgumentException e) {
                        if (PropertyLoader.getSystemBooleanProperty("parser.skip_broken_blocks")) {
                            log.warning(
                                    "Skipping block with ID '"
                                            + currentId.id()
                                            + "' and opcode '"
                                            + opcode
                                            + "'. Ignoring this block. Problem: "
                                            + e.getMessage()
                            );
                        } else {
                            throw e;
                        }
                    }

                    currentId = regularBlock.next().orElse(null);
                    currentBlock = state.getBlock(currentId);
                }
            }
        }

        return new StmtList(Collections.unmodifiableList(stmts));
    }

    private static boolean isIgnoredBlockForStmtListConverter(final String opcode) {
        return DependentBlockOpcode.contains(opcode)
                || ProcedureOpcode.procedures_definition.name().equals(opcode)
                || ProcedureOpcode.procedures_prototype.name().equals(opcode);
    }

    private static boolean isShadowParameter(final RawBlock.RawRegularBlock block) {
        return block.shadow()
                && (
                    ProcedureOpcode.argument_reporter_string_number.name().equals(block.opcode())
                            || ProcedureOpcode.argument_reporter_boolean.name().equals(block.opcode())
                );
    }
}

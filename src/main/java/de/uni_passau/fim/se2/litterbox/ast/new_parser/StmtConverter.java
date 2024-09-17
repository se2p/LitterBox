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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawField;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

import java.util.Collections;

abstract class StmtConverter<T extends Stmt> {

    protected final ProgramParserState state;

    protected StmtConverter(final ProgramParserState state) {
        this.state = state;
    }

    abstract T convertStmt(RawBlockId blockId, RawBlock.RawRegularBlock block);

    protected Qualified getOrCreateReferencedVariable(final RawBlock.RawRegularBlock stmtBlock) {
        final RawField varField = stmtBlock.fields().get(Constants.VARIABLE_KEY);
        final String varName = varField.value().toString();
        final RawBlockId varId = varField.id()
                .orElseThrow(() -> new InternalParsingException("Missing variable id."));

        final VariableInfo varInfo = state.getSymbolTable().getOrAddVariable(
                varId.id(), varName, state.getCurrentActor().getName(),
                StringType::new, true, "Stage"
        );

        return new Qualified(new StrId(varInfo.getActor()), new Variable(new StrId(varName)));
    }

    protected Qualified getOrCreateReferencedList(final RawBlock.RawRegularBlock stmtBlock) {
        final RawField listField = stmtBlock.fields().get(Constants.LIST_KEY);
        final String listName = listField.value().toString();
        final RawBlockId listId = listField.id()
                .orElseThrow(() -> new InternalParsingException("Missing variable id."));

        final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                listId.id(), listName, state.getCurrentActor().getName(),
                () -> new ExpressionList(Collections.emptyList()), true, "Stage"
        );

        return new Qualified(new StrId(listInfo.getActor()), new ScratchList(new StrId(listName)));
    }
}

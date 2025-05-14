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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.KnownFields;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawField;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

import java.util.Collections;
import java.util.Optional;

abstract class StmtConverter<T extends Stmt> {

    protected final ProgramParserState state;

    protected StmtConverter(final ProgramParserState state) {
        this.state = state;
    }

    abstract T convertStmt(RawBlockId blockId, RawBlock.RawRegularBlock block);

    protected Qualified getOrCreateReferencedVariable(final RawBlock.RawRegularBlock stmtBlock) {
        final RawField varField = stmtBlock.getField(KnownFields.VARIABLE);
        final String varName = varField.value().toString();
        final Optional<RawBlockId> optVarId = varField.id();
        final RawBlockId varId;
        if (optVarId.isPresent()) {
            varId = optVarId.get();
        } else {
            String id = state.getSymbolTable().getVariableIdentifierFromActorAndName(state.getCurrentActor().getName(), varName);
            if (id != null) {
                varId = new RawBlockId(id);
            } else {
                throw new InternalParsingException("Missing variable id.");
            }
        }

        final VariableInfo varInfo = state.getSymbolTable().getOrAddVariable(
                varId.id(), varName, state.getCurrentActor().getName(),
                StringType::new, true, "Stage"
        );

        return ConverterUtilities.variableInfoToIdentifier(varInfo, varName);
    }

    protected Qualified getOrCreateReferencedList(final RawBlock.RawRegularBlock stmtBlock) {
        final RawField listField = stmtBlock.getField(KnownFields.LIST);
        final String listName = listField.value().toString();
        final Optional<RawBlockId> optListId = listField.id();

        final RawBlockId listId;
        if (optListId.isPresent()) {
            listId = optListId.get();
        } else {
            String id = state.getSymbolTable().getListIdentifierFromActorAndName(state.getCurrentActor().getName(), listName);
            if (id != null) {
                listId = new RawBlockId(id);
            } else {
                throw new InternalParsingException("Missing variable id.");
            }
        }

        final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                listId.id(), listName, state.getCurrentActor().getName(),
                () -> new ExpressionList(Collections.emptyList()), true, "Stage"
        );

        return ConverterUtilities.listInfoToIdentifier(listInfo, listName);
    }
}

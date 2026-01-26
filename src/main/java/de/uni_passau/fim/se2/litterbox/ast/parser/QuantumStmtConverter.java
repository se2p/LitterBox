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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.QuantumOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;

final class QuantumStmtConverter extends StmtConverter<Stmt> {

    QuantumStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    Stmt convertStmt(RawBlockId blockId, RawBlock.RawRegularBlock block) {
        final QuantumOpcode opcode = QuantumOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case quantum_measure -> new Measure(metadata);
            case quantum_superposition_no_list -> {
                final FixedQuantumAttribute attribute = convertAttribute(block.getField(KnownFields.VARIABLES));
                final NumExpr possibilities = NumExprConverter.convertNumExpr(state, block, KnownInputs.N_CLONES);
                yield new QuantumSuperpositionPossibilities(attribute, possibilities, metadata);
            }
            case quantum_superposition_only_list -> {
                final FixedQuantumAttribute attribute = convertAttribute(block.getField(KnownFields.VARIABLES));
                final Expression list = ExprConverter.convertExpr(state, block, KnownInputs.LISTA);
                yield new QuantumSuperpositionList(attribute, list, metadata);
            }
            case quantum_superpositions -> {
                final FixedQuantumAttribute attribute = convertAttribute(block.getField(KnownFields.VARIABLES));
                final NumExpr possibilities = NumExprConverter.convertNumExpr(state, block, KnownInputs.N_CLONES);
                final Expression list = ExprConverter.convertExpr(state, block, KnownInputs.LISTA);
                yield new QuantumSuperpositionPossibilitiesList(attribute, possibilities, list, metadata);
            }
            case quantum_entanglement_no_list -> {
                final FixedQuantumAttribute attribute = convertAttribute(block.getField(KnownFields.VARIABLES));
                final Expression target = ExprConverter.convertExpr(state, block, KnownInputs.TARGET);
                final NumExpr possibilities = NumExprConverter.convertNumExpr(state, block, KnownInputs.N_CLONES);
                yield new QuantumEntanglementPossibilities(attribute, target, possibilities, metadata);
            }
            case quantum_entanglement_only_list -> {
                final FixedQuantumAttribute attribute = convertAttribute(block.getField(KnownFields.VARIABLES));
                final Expression target = ExprConverter.convertExpr(state, block, KnownInputs.TARGET);
                final Expression list = ExprConverter.convertExpr(state, block, KnownInputs.LISTA1);
                yield new QuantumEntanglementList(attribute, target, list, metadata);
            }
            case quantum_entanglement -> {
                final FixedQuantumAttribute attribute = convertAttribute(block.getField(KnownFields.VARIABLES));
                final Expression target = ExprConverter.convertExpr(state, block, KnownInputs.TARGET);
                final NumExpr possibilities = NumExprConverter.convertNumExpr(state, block, KnownInputs.LISTA1);
                final Expression list = ExprConverter.convertExpr(state, block, KnownInputs.LISTA2);
                yield new QuantumEntanglementPossibilitiesList(attribute, target, possibilities, list, metadata);
            }
        };
    }

    private FixedQuantumAttribute convertAttribute(final RawField field) {
        if (field.value() instanceof String type) {
            return new FixedQuantumAttribute(FixedQuantumAttribute.FixedQuantumAttributeType.fromString(type));
        } else {
            throw new InternalParsingException("Unknown quantum field type: " + field.value());
        }
    }
}

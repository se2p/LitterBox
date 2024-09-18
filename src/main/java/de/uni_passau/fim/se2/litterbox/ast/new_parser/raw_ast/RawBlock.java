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
package de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;
import java.util.Optional;

@JsonDeserialize(using = RawBlockDeserializer.class)
public sealed interface RawBlock {
    @JsonDeserialize(as = RawRegularBlock.class)
    record RawRegularBlock(
            String opcode,
            Optional<RawBlockId> next,
            Optional<RawBlockId> parent,
            Map<String, RawInput> inputs,
            Map<String, RawField> fields,
            boolean shadow,
            boolean topLevel,
            double x,
            double y,
            Optional<RawBlockId> comment,
            Optional<RawMutation> mutation
    ) implements RawBlock {}

    sealed interface ArrayBlock extends RawBlock {}

    record RawFloatBlockLiteral(double value) implements ArrayBlock {}

    record RawIntBlockLiteral(long value) implements ArrayBlock {}

    record RawAngleBlockLiteral(double angle) implements ArrayBlock {}

    record RawColorLiteral(String color) implements ArrayBlock {}

    record RawStringLiteral(String value) implements ArrayBlock {}

    record RawBroadcast(String name, RawBlockId id) implements ArrayBlock {}

    record RawVariable(String name, RawBlockId id, Optional<Coordinates> coordinates) implements ArrayBlock {}

    record RawList(String name, RawBlockId id, Optional<Coordinates> coordinates) implements ArrayBlock {}
}

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
package de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

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
    ) implements RawBlock {
        public boolean hasField(final KnownFields field) {
            return fields.containsKey(field.getName());
        }

        public boolean hasInput(final KnownInputs input) {
            return inputs.containsKey(input.getName());
        }

        /**
         * Gets the block field.
         *
         * @param field The type of the field.
         * @return The field, if this block has it.
         * @throws IllegalArgumentException In case this block does not have the requested field.
         */
        public RawField getField(final KnownFields field) throws IllegalArgumentException {
            final RawField f = fields.get(field.getName());

            if (f == null) {
                throw new IllegalArgumentException("Block '" + opcode + "' has no field '" + field.getName() + "'.");
            }

            return f;
        }

        /**
         * Gets the field value as String.
         *
         * <p>Most block fields values are stored as String anyway. For those, no conversion is necessary. Regardless,
         * in case a conversion is required, this will be done using the {@link Object#toString()} method.
         *
         * <p>Implementation note: Enabling {@link java.util.logging.Level#FINE} logging logs cases when the internal
         * conversion to String is performed.
         *
         * @param field The type of th field.
         * @return The field value as String, potentially converted to String from another type.
         * @throws IllegalArgumentException In case this block does not have the requested field.
         */
        public String getFieldValueAsString(final KnownFields field) throws IllegalArgumentException {
            final RawField f = getField(field);

            if (f.value() instanceof String s) {
                return s;
            } else {
                Logger.getGlobal().fine(
                        String.format(
                                "Converting field '%s' of block '%s' to String. Was '%s'.",
                                field.getName(), opcode, f.value().getClass().getSimpleName()
                        )
                );
                return f.value().toString();
            }
        }

        /**
         * Gets the block input.
         *
         * @param input The type of the input.
         * @return The input, if this block has it.
         * @throws IllegalArgumentException In case this block does not have the requested input.
         */
        public RawInput getInput(final KnownInputs input) throws IllegalArgumentException {
            final RawInput i = inputs.get(input.getName());

            if (i == null) {
                throw new IllegalArgumentException("Block '" + opcode + "' has no input '" + input.getName() + "'.");
            }

            return i;
        }

        public Optional<RawInput> getOptionalInput(final KnownInputs input) {
            return Optional.ofNullable(inputs.get(input.getName()));
        }

    }

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

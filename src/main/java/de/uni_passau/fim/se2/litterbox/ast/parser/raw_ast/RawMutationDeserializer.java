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
package de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast;

import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ValueNode;

import java.util.Collections;
import java.util.List;

final class RawMutationDeserializer {
    private RawMutationDeserializer() {
        throw new IllegalCallerException("utility class");
    }

    static final class ArgumentIdsParser extends ValueDeserializer<List<RawBlockId>> {

        @Override
        public List<RawBlockId> deserialize(JsonParser p, DeserializationContext ctxt) {
            final ValueNode root = p.readValueAsTree();

            if (root.isNull()) {
                return Collections.emptyList();
            } else {
                try (final var parser = ctxt.createParser(root.asString())) {
                    return parser.readValueAs(new TypeReference<>() {
                    });
                }
            }
        }
    }

    static final class ArgumentNamesParser extends ValueDeserializer<List<String>> {

        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) {
            final ValueNode root = p.readValueAsTree();

            if (root.isNull()) {
                return Collections.emptyList();
            } else {
                try (final var parser = ctxt.createParser(root.asString())) {
                    return parser.readValueAs(new TypeReference<>() {
                    });
                }
            }
        }
    }

    static final class ArgumentDefaultValuesParser
            extends ValueDeserializer<List<? extends RawMutation.ArgumentDefault<?>>> {

        @Override
        public List<? extends RawMutation.ArgumentDefault<?>> deserialize(
                JsonParser p, DeserializationContext ctxt
        ) {
            final ValueNode root = p.readValueAsTree();

            if (root.isNull()) {
                return Collections.emptyList();
            } else {
                try (final var parser = ctxt.createParser(root.asString())) {
                    final List<Object> values = parser.readValueAs(new TypeReference<>() {
                    });
                    return values.stream().map(ArgumentDefaultValuesParser::getDefaultValue).toList();
                }
            }
        }

        private static RawMutation.ArgumentDefault<?> getDefaultValue(final Object item) {
            return switch (item) {
                case Boolean b -> new RawMutation.ArgumentDefault.BoolArgumentDefault(b);
                case String s -> new RawMutation.ArgumentDefault.StringArgumentDefault(s);
                case Integer i -> new RawMutation.ArgumentDefault.NumArgumentDefault(i.doubleValue());
                case Double d -> new RawMutation.ArgumentDefault.NumArgumentDefault(d);
                default -> throw new IllegalArgumentException("Unknown procedure default type: " + item);
            };
        }
    }
}

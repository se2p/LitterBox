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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

final class RawMutationDeserializer {
    private RawMutationDeserializer() {
        throw new IllegalCallerException("utility class");
    }

    static final class ArgumentIdsParser extends JsonDeserializer<List<RawBlockId>> {

        @Override
        public List<RawBlockId> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final ValueNode root = p.getCodec().readTree(p);

            if (root.isNull()) {
                return Collections.emptyList();
            } else {
                return mapper.readValue(root.textValue(), new TypeReference<>() {});
            }
        }
    }

    static final class ArgumentNamesParser extends JsonDeserializer<List<String>> {

        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final ValueNode root = p.getCodec().readTree(p);

            if (root.isNull()) {
                return Collections.emptyList();
            } else {
                return mapper.readValue(root.textValue(), new TypeReference<>() {});
            }
        }
    }

    static final class ArgumentDefaultValuesParser
            extends JsonDeserializer<List<? extends RawMutation.ArgumentDefault<?>>> {

        @Override
        public List<? extends RawMutation.ArgumentDefault<?>> deserialize(
                JsonParser p, DeserializationContext ctxt
        ) throws IOException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final ValueNode root = p.getCodec().readTree(p);

            if (root.isNull()) {
                return Collections.emptyList();
            } else {
                final List<Object> values = mapper.readValue(root.textValue(), new TypeReference<>() {});
                return values.stream().map(ArgumentDefaultValuesParser::getDefaultValue).toList();
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

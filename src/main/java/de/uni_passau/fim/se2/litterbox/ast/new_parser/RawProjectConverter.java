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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class RawProjectConverter {
    private final RawProject project;
    private final String projectName;
    private final ProgramParserState parserState;

    private RawProjectConverter(final RawProject project, final String projectName) {
        this.project = project;
        this.projectName = projectName;
        this.parserState = new ProgramParserState();
    }

    static Program convert(final RawProject project, final String projectName) throws ParsingException {
        try {
            final RawProjectConverter converter = new RawProjectConverter(project, projectName);
            return converter.convert();
        } catch (InternalParsingException | IllegalArgumentException e) {
            throw new ParsingException("Failed to convert project '" + projectName + "': " + e.getMessage(), e);
        }
    }

    Program convert() {
        final RawTargetConverter rawTargetConverter = new RawTargetConverter(parserState);

        final LocalIdentifier id = new StrId(projectName);
        final List<ActorDefinition> actors = project.targets().stream().map(rawTargetConverter::convertTarget).toList();
        final ActorDefinitionList actorList = new ActorDefinitionList(actors);
        final ProgramMetadata metadata = convertMetadata();

        return new Program(id, actorList, parserState.getSymbolTable(), parserState.getProcDefMap(), metadata);
    }

    private ProgramMetadata convertMetadata() {
        final List<MonitorMetadata> monitorMetadataList = project.monitors().stream()
                .map(this::convertMonitor)
                .toList();
        final MonitorMetadataList monitorMetadata = new MonitorMetadataList(monitorMetadataList);
        final ExtensionMetadata extensionMetadata = new ExtensionMetadata(project.extensions());
        final MetaMetadata metaMetadata = new MetaMetadata(
                project.meta().semver(), project.meta().vm(), project.meta().agent()
        );

        return new ProgramMetadata(monitorMetadata, extensionMetadata, metaMetadata);
    }

    private MonitorMetadata convertMonitor(final RawMonitor monitor) {
        final Map<String, String> params = Objects.requireNonNullElse(monitor.params(), Collections.emptyMap());
        final MonitorParamMetadataList paramMeta = new MonitorParamMetadataList(
               params.entrySet().stream()
                        .map(entry -> new MonitorParamMetadata(entry.getKey(), entry.getValue()))
                        .toList()
        );

        if (monitor.value() instanceof List<?> values) {
            final List<String> items = values.stream().map(v -> {
                if (v instanceof String s) {
                    return s;
                } else if (v instanceof Double d) {
                    return Double.toString(d);
                } else if (v instanceof Integer i) {
                    return Integer.toString(i);
                } else if (v instanceof Long l) {
                    return Long.toString(l);
                } else if (v instanceof BigInteger i) {
                    return i.toString();
                } else if (v instanceof BigDecimal d) {
                    return d.toPlainString();
                } else if (v instanceof Boolean b) {
                    return b.toString();
                } else if (v == null) {
                    return "null";
                } else {
                    throw new InternalParsingException("Monitor values list contains unexpected item: " + v);
                }
            }).toList();

            return new MonitorListMetadata(
                    monitor.id(),
                    monitor.mode().getMode(),
                    monitor.opcode(),
                    paramMeta,
                    monitor.spriteName(),
                    monitor.width(),
                    monitor.height(),
                    monitor.x(),
                    monitor.y(),
                    monitor.visible(),
                    items
            );
        } else {
            final Object v = monitor.value();
            if (!isValidMonitorValue(v)) {
                throw new InternalParsingException("Unknown monitor type! Neither list nor regular value.");
            }

            return new MonitorSliderMetadata(
                    monitor.id(),
                    monitor.mode().getMode(),
                    monitor.opcode(),
                    paramMeta,
                    monitor.spriteName(),
                    monitor.width(),
                    monitor.height(),
                    monitor.x(),
                    monitor.y(),
                    monitor.visible(),
                    monitor.value().toString(),
                    Objects.requireNonNullElse(monitor.sliderMin(), 0.0),
                    Objects.requireNonNullElse(monitor.sliderMax(), 100.0),
                    monitor.isDiscrete()
            );
        }
    }

    private static boolean isValidMonitorValue(final Object v) {
        return v instanceof String
                || v instanceof Boolean
                || v instanceof Integer
                || v instanceof Double
                || v instanceof Long
                || v instanceof BigInteger
                || v instanceof BigDecimal;
    }
}

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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public record RawMonitor(
        String id,
        RawMonitorMode mode,
        String opcode,
        Map<String, String> params,
        String spriteName,
        Object value,
        int width,
        int height,
        int x,
        int y,
        boolean visible,
        @JsonAlias({"min", "sliderMin"})
        Double sliderMin,
        @JsonAlias({"max", "sliderMax"})
        Double sliderMax,
        boolean isDiscrete
) {
    public enum RawMonitorMode {
        DEFAULT("default"),
        LARGE("large"),
        SLIDER("slider"),
        LIST("list");

        private final String mode;

        RawMonitorMode(final String mode) {
            this.mode = mode;
        }

        @JsonValue
        public String getMode() {
            return mode;
        }
    }
}

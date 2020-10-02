/*
 * Copyright (C) 2020 LitterBox contributors
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

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELDS_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPCODE_KEY;

public class TimecompParser {

    private static final String CURRENT_OPCODE = "sensing_current";
    private static final String CURRENT_MENU = "CURRENTMENU";

    public static TimeComp parse(JsonNode current) throws ParsingException {
        Preconditions.checkNotNull(current);
        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions.checkArgument(opcodeString.equals(CURRENT_OPCODE),
                "Timecomp parsing is only allowed for opcode %s and not %s", CURRENT_OPCODE, opcodeString);

        final String currentString = current.get(FIELDS_KEY).get(CURRENT_MENU).get(0).asText();
        return new TimeComp(currentString.toLowerCase());
    }
}

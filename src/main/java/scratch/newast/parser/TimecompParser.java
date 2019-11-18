/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.timecomp.Date;
import scratch.newast.model.timecomp.DayOfWeek;
import scratch.newast.model.timecomp.Hour;
import scratch.newast.model.timecomp.Minute;
import scratch.newast.model.timecomp.Month;
import scratch.newast.model.timecomp.Second;
import scratch.newast.model.timecomp.TimeComp;
import scratch.newast.model.timecomp.Year;

public class TimecompParser {

    public static final String CURRENT_OPCODE = "sensing_current";
    public static final String CURRENT_MENU = "CURRENT_MENU";

    public static TimeComp parse(JsonNode current) throws ParsingException {
        Preconditions.checkNotNull(current);
        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions.checkArgument(opcodeString.equals(CURRENT_OPCODE),
            "Timecomp parsing is only allowed for opcode %s and not %s", CURRENT_OPCODE, opcodeString);

        String currentString = current.get(FIELDS_KEY).get(CURRENT_MENU).get(0).asText();
        switch (currentString) {
            case "YEAR":
                return new Year();
            case "MONTH":
                return new Month();
            case "DATE":
                return new Date();
            case "DAYOFWEEK":
                return new DayOfWeek();
            case "HOUR":
                return new Hour();
            case "MINUTE":
                return new Minute();
            case "SECOND":
                return new Second();
            default:
                throw new ParsingException("No timecomp for value " + currentString);
        }
    }
}

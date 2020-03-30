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
package de.uni_passau.fim.se2.litterbox.ast.parser.symboltable;

import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Identifier;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProcedureDefinitionNameMapping {

    private Map<String, Map<Identifier, ProcedureInfo>> procedures;

    public ProcedureDefinitionNameMapping() {
        procedures = new LinkedHashMap<>();
    }

    public void addProcedure(Identifier identifier, String actorName, String procedureName, String[] argumentNames,
                             Type[] types) {
        Map<Identifier, ProcedureInfo> currentMap;
        if (procedures.containsKey(actorName)) {
            currentMap = procedures.get(actorName);
        } else {
            currentMap = new LinkedHashMap<>();
            procedures.put(actorName, currentMap);
        }
        currentMap.put(identifier, new ProcedureInfo(procedureName, makeArguments(argumentNames, types), actorName));
    }

    private ArgumentInfo[] makeArguments(String[] argumentNames, Type[] types) {
        Preconditions.checkArgument(argumentNames.length == types.length);
        ArgumentInfo[] arguments = new ArgumentInfo[argumentNames.length];
        for (int i = 0; i < argumentNames.length; i++) {
            arguments[i] = new ArgumentInfo(argumentNames[i], types[i]);
        }
        return arguments;
    }

    public Map<String, Map<Identifier, ProcedureInfo>> getProcedures() {
        return procedures;
    }
}

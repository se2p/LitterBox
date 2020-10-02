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
package de.uni_passau.fim.se2.litterbox.ast.parser.symboltable;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcedureDefinitionNameMapping {

    private Map<String, Map<LocalIdentifier, ProcedureInfo>> procedures;
    private List<String> malformatedProcedures;

    public ProcedureDefinitionNameMapping() {
        procedures = new LinkedHashMap<>();
        malformatedProcedures = new ArrayList<>();
    }

    public ProcedureDefinitionNameMapping(ProcedureDefinitionNameMapping other) {
        procedures = new LinkedHashMap<>();
        // TODO: This creates a deep copy of the datastructure itself, but the LocalIdentifier and ProcedureInfo
        //       are not copied
        for (Map.Entry<String, Map<LocalIdentifier, ProcedureInfo>> entry : other.procedures.entrySet()) {
            procedures.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }
        malformatedProcedures = new ArrayList<>(other.malformatedProcedures);
    }

    public void addProcedure(LocalIdentifier localIdentifier,
                             String actorName,
                             String procedureName,
                             String[] argumentNames,
                             Type[] types) {

        Map<LocalIdentifier, ProcedureInfo> currentMap;
        if (procedures.containsKey(actorName)) {
            currentMap = procedures.get(actorName);
        } else {
            currentMap = new LinkedHashMap<>();
            procedures.put(actorName, currentMap);
        }
        currentMap.put(localIdentifier,
                new ProcedureInfo(procedureName, makeArguments(argumentNames, types), actorName));
    }

    private ArgumentInfo[] makeArguments(String[] argumentNames, Type[] types) {
        Preconditions.checkArgument(argumentNames.length == types.length);
        ArgumentInfo[] arguments = new ArgumentInfo[argumentNames.length];
        for (int i = 0; i < argumentNames.length; i++) {
            arguments[i] = new ArgumentInfo(argumentNames[i], types[i]);
        }
        return arguments;
    }

    public Map<String, Map<LocalIdentifier, ProcedureInfo>> getProcedures() {
        return procedures;
    }

    public ProcedureInfo getProcedureForHash(String actorName, String jsonHash) {
        Map<LocalIdentifier, ProcedureInfo> procedureMap = getProcedures().get(actorName);
        return procedureMap.entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals(jsonHash))
                .map(Map.Entry::getValue)
                .findFirst().get();
    }

    public ProcedureInfo getProcedureForName(String actorName, String jsonHash) {
        Map<LocalIdentifier, ProcedureInfo> procedureMap = getProcedures().get(actorName);
        return procedureMap.values().stream().filter(p -> p.getName().equals(jsonHash)).findFirst().get();
    }

    public void addMalformated(String malformated) {
        malformatedProcedures.add(malformated);
    }

    public boolean checkIfMalformated(String toCheck) {
        return malformatedProcedures.contains(toCheck);
    }
}

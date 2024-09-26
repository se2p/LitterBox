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
package de.uni_passau.fim.se2.litterbox.ast.parser.symboltable;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ProcedureDefinitionNameMapping {

    private final Map<String, Map<LocalIdentifier, ProcedureInfo>> procedures;
    private final List<String> malformedProcedures;

    public ProcedureDefinitionNameMapping() {
        procedures = new LinkedHashMap<>();
        malformedProcedures = new ArrayList<>();
    }

    public ProcedureDefinitionNameMapping(ProcedureDefinitionNameMapping other) {
        procedures = new LinkedHashMap<>();
        // TODO: This creates a deep copy of the datastructure itself, but the LocalIdentifier and ProcedureInfo
        //       are not copied
        for (Map.Entry<String, Map<LocalIdentifier, ProcedureInfo>> entry : other.procedures.entrySet()) {
            procedures.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }
        malformedProcedures = new ArrayList<>(other.malformedProcedures);
    }

    public void addProcedure(LocalIdentifier localIdentifier,
                             String actorName,
                             String procedureName,
                             String[] argumentNames,
                             Type[] types) throws ParsingException {
        final ProcedureInfo info = new ProcedureInfo(procedureName, makeArguments(argumentNames, types), actorName);

        addProcedureForActor(actorName, localIdentifier, info);
    }

    public void addProcedure(
            final LocalIdentifier ident,
            final String actorName,
            final String procedureName,
            final ParameterDefinitionList parameters
    ) {
        final ArgumentInfo[] arguments = parameters.getParameterDefinitions().stream()
                .map(parameterDefinition -> new ArgumentInfo(
                        parameterDefinition.getIdent().getName(), parameterDefinition.getType()
                ))
                .toArray(ArgumentInfo[]::new);
        final ProcedureInfo info = new ProcedureInfo(procedureName, arguments, actorName);

        addProcedureForActor(actorName, ident, info);
    }

    private void addProcedureForActor(
            final String actorName, final LocalIdentifier identifier, final ProcedureInfo procedure
    ) {
        procedures.compute(actorName, (actor, actorProcedureMap) -> {
            if (actorProcedureMap == null) {
                actorProcedureMap = new LinkedHashMap<>();
            }

            actorProcedureMap.put(identifier, procedure);

            return actorProcedureMap;
        });
    }

    private ArgumentInfo[] makeArguments(String[] argumentNames, Type[] types) throws ParsingException {
        if (argumentNames.length != types.length) {
            throw new ParsingException("The project has a custom block with problems in its definition.");
        }
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
        Map<LocalIdentifier, ProcedureInfo> procedureMap = getProceduresForActor(actorName);
        return procedureMap.entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals(jsonHash))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Finds a procedure in the given actor with the requested name.
     *
     * <p><em>Note:</em>
     * Scratch supports overloading procedure names.
     * This method will return any of them.
     * Use {@link #getProceduresForName(String, String)} to get all procedures with the requested name.
     *
     * @param actorName The name of the actor the procedure is defined in.
     * @param name The name of the procedure.
     * @return A procedure with the requested name.
     */
    public Optional<ProcedureInfo> getProcedureForName(String actorName, String name) {
        final Map<LocalIdentifier, ProcedureInfo> procedureMap = getProceduresForActor(actorName);
        return procedureMap.values().stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    public List<Pair<LocalIdentifier, ProcedureInfo>> getProceduresForName(String actorName, String name) {
        final Map<LocalIdentifier, ProcedureInfo> procedureMap = getProceduresForActor(actorName);
        return procedureMap.entrySet()
                .stream()
                .filter(e -> e.getValue().getName().equals(name))
                .map(Pair::of)
                .toList();
    }

    public ProcedureInfo getProcedureInfo(final ProcedureDefinition procedureDefinition) {
        final ActorDefinition actor = AstNodeUtil.findActor(procedureDefinition)
                .orElseThrow(() -> new IllegalStateException("Invalid AST: Could not find actor for procedure."));
        final String hash =  procedureDefinition.getIdent().getName();
        return getProcedureForHash(actor.getIdent().getName(), hash);
    }

    public Map<LocalIdentifier, ProcedureInfo> getProceduresForActor(final String actorName) {
        return getProcedures().getOrDefault(actorName, Collections.emptyMap());
    }

    public void addMalformed(String malformed) {
        malformedProcedures.add(malformed);
    }

    public boolean checkIfMalformed(String toCheck) {
        return malformedProcedures.contains(toCheck);
    }
}

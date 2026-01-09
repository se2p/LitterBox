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
package de.uni_passau.fim.se2.litterbox.diff;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.*;
import java.util.stream.Collectors;

public class CodeDiffGenerator {

    public ProjectCodeDiff generate(Program before, Program after) {
        List<ActorCodeDiff> actorDiffs = new ArrayList<>();

        // Map actors by name
        Map<String, ActorDefinition> beforeActors = AstNodeUtil.getActors(before, true)
                .collect(Collectors.toMap(a -> a.getIdent().getName(), a -> a));
        Map<String, ActorDefinition> afterActors = AstNodeUtil.getActors(after, true)
                .collect(Collectors.toMap(a -> a.getIdent().getName(), a -> a));

        Set<String> allActorNames = new HashSet<>();
        allActorNames.addAll(beforeActors.keySet());
        allActorNames.addAll(afterActors.keySet());

        // Sort actor names for deterministic order (Stage first, then alphabetical)
        List<String> sortedNames = new ArrayList<>(allActorNames);
        sortedNames.sort((n1, n2) -> {
            boolean s1 = beforeActors.containsKey(n1) ? beforeActors.get(n1).isStage() : afterActors.get(n1).isStage();
            boolean s2 = beforeActors.containsKey(n2) ? beforeActors.get(n2).isStage() : afterActors.get(n2).isStage();
            if (s1 && !s2) {
                return -1;
            }
            if (!s1 && s2) {
                return 1;
            }
            return n1.compareTo(n2);
        });

        for (String name : sortedNames) {
            ActorDefinition beforeActor = beforeActors.get(name);
            ActorDefinition afterActor = afterActors.get(name);
            actorDiffs.add(diffActors(name, beforeActor, afterActor));
        }

        // Filter out actors with no diffs (ProjectCodeDiff.isEmpty() relies on ActorCodeDiff.hasResultingDiffs())
        // but we want to include the actor in the list if there are diffs.
        List<ActorCodeDiff> filteredDiffs = actorDiffs.stream()
                .filter(ActorCodeDiff::hasResultingDiffs)
                .toList();

        return new ProjectCodeDiff(filteredDiffs);
    }

    private ActorCodeDiff diffActors(String name, ActorDefinition beforeActor, ActorDefinition afterActor) {
        List<ScriptCodeDiff> scriptDiffs = new ArrayList<>();
        boolean isStage = (beforeActor != null && beforeActor.isStage()) || (afterActor != null && afterActor.isStage());

        if (beforeActor == null) {
            // All scripts in 'after' are ADDED
            if (afterActor != null) {
                afterActor.getScripts().getScriptList()
                        .forEach(script -> scriptDiffs.add(new ScriptCodeDiff(null, generateCode(script), ScriptChangeType.ADDED)));
            }
        } else if (afterActor == null) {
            // All scripts in 'before' are DELETED
            beforeActor.getScripts().getScriptList()
                    .forEach(script -> scriptDiffs.add(new ScriptCodeDiff(generateCode(script), null, ScriptChangeType.DELETED)));
        } else {
            // Compare scripts
            scriptDiffs.addAll(diffScripts(beforeActor, afterActor));
        }

        return new ActorCodeDiff(name, isStage, scriptDiffs);
    }

    private List<ScriptCodeDiff> diffScripts(ActorDefinition beforeActor, ActorDefinition afterActor) {
        List<ScriptCodeDiff> diffs = new ArrayList<>();

        // Helper maps and lists
        Map<String, Script> beforeScriptsById = new HashMap<>();
        List<Script> beforeUnmatched = new ArrayList<>();

        beforeActor.getScripts().getScriptList().forEach(s -> {
            String id = AstNodeUtil.getBlockId(s);
            if (id != null) {
                beforeScriptsById.put(id, s);
            } else {
                beforeUnmatched.add(s);
            }
        });

        Map<String, Script> afterScriptsById = new HashMap<>();
        List<Script> afterUnmatched = new ArrayList<>();

        afterActor.getScripts().getScriptList().forEach(s -> {
            String id = AstNodeUtil.getBlockId(s);
            if (id != null) {
                afterScriptsById.put(id, s);
            } else {
                afterUnmatched.add(s);
            }
        });

        Set<String> processedAfterIds = new HashSet<>();
        Set<Script> processedAfterScripts = new HashSet<>();

        // 1. Match by ID
        for (Map.Entry<String, Script> entry : beforeScriptsById.entrySet()) {
            String id = entry.getKey();
            Script beforeScript = entry.getValue();

            if (afterScriptsById.containsKey(id)) {
                Script afterScript = afterScriptsById.get(id);
                diffs.add(createDiff(beforeScript, afterScript));
                processedAfterIds.add(id);
            } else {
                beforeUnmatched.add(beforeScript); // Move to unmatched for structural fallback
            }
        }

        // 2. Fallback: Structural Equality (Exact)
        Iterator<Script> beforeIt = beforeUnmatched.iterator();
        while (beforeIt.hasNext()) {
            Script beforeScript = beforeIt.next();
            Optional<Script> match = findStructuralMatch(beforeScript, afterScriptsById, afterUnmatched, processedAfterIds, processedAfterScripts, false);

            if (match.isPresent()) {
                diffs.add(createDiff(beforeScript, match.get()));
                beforeIt.remove();
            }
        }

        // 3. Fallback: Structural Equality (Ignore Literals) -> Likely CHANGED
        beforeIt = beforeUnmatched.iterator();
        while (beforeIt.hasNext()) {
            Script beforeScript = beforeIt.next();
            Optional<Script> match = findStructuralMatch(beforeScript, afterScriptsById, afterUnmatched, processedAfterIds, processedAfterScripts, true);

            if (match.isPresent()) {
                diffs.add(createDiff(beforeScript, match.get()));
                beforeIt.remove();
            }
        }

        // 4. Remaining Before -> DELETED
        for (Script beforeScript : beforeUnmatched) {
            diffs.add(new ScriptCodeDiff(generateCode(beforeScript), null, ScriptChangeType.DELETED));
        }

        // 5. Remaining After (ID based) -> ADDED
        for (Map.Entry<String, Script> entry : afterScriptsById.entrySet()) {
            if (!processedAfterIds.contains(entry.getKey())) {
                diffs.add(new ScriptCodeDiff(null, generateCode(entry.getValue()), ScriptChangeType.ADDED));
            }
        }

        // 6. Remaining After (Unmatched list) -> ADDED
        for (Script afterScript : afterUnmatched) {
            if (!processedAfterScripts.contains(afterScript)) {
                diffs.add(new ScriptCodeDiff(null, generateCode(afterScript), ScriptChangeType.ADDED));
            }
        }

        return diffs;
    }

    private Optional<Script> findStructuralMatch(
            Script target,
            Map<String, Script> afterScriptsById,
            List<Script> afterUnmatched,
            Set<String> processedAfterIds,
            Set<Script> processedAfterScripts,
            boolean ignoreLiterals
    ) {
        // Check in ID-mapped scripts that haven't been processed yet
        for (Map.Entry<String, Script> entry : afterScriptsById.entrySet()) {
            if (!processedAfterIds.contains(entry.getKey())) {
                Script candidate = entry.getValue();
                boolean isEqual = ignoreLiterals
                        ? StructuralEquality.areStructurallyEqualIgnoringLiterals(target, candidate)
                        : StructuralEquality.areStructurallyEqual(target, candidate);

                if (isEqual) {
                    processedAfterIds.add(entry.getKey());
                    return Optional.of(candidate);
                }
            }
        }

        // Check in unmatched scripts
        for (Script candidate : afterUnmatched) {
            if (!processedAfterScripts.contains(candidate)) {
                boolean isEqual = ignoreLiterals
                        ? StructuralEquality.areStructurallyEqualIgnoringLiterals(target, candidate)
                        : StructuralEquality.areStructurallyEqual(target, candidate);

                if (isEqual) {
                    processedAfterScripts.add(candidate);
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }

    private ScriptCodeDiff createDiff(Script before, Script after) {
        String beforeCode = generateCode(before);
        String afterCode = generateCode(after);

        ScriptChangeType type = ScriptChangeType.UNCHANGED;
        if (!Objects.equals(beforeCode, afterCode)) {
            type = ScriptChangeType.CHANGED;
        }

        return new ScriptCodeDiff(beforeCode, afterCode, type);
    }

    private String generateCode(Script script) {
        return ScratchBlocksVisitor.ofIgnoringLooseBlocks(script);
    }
}

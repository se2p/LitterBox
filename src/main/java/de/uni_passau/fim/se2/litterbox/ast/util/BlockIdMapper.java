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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;
import java.util.function.Predicate;

/**
 * Builds a mapping of block IDs between two structurally equivalent AST nodes.
 *
 * <p>This is useful for comparing programs where block IDs have been regenerated
 * (e.g., after re-serialization) but the structure remains the same. The mapping
 * allows translating "before" block IDs to "after" block IDs.
 *
 * <p>The mapping is only built if the two nodes are structurally equal (as defined
 * by {@link StructuralEquality}). If they are not equal, an empty map is returned.
 */
public final class BlockIdMapper {

    private BlockIdMapper() {
        // Utility class
    }

    /**
     * Builds a block ID mapping between two programs.
     *
     * <p>For each actor in the before program, finds the corresponding actor
     * in the after program (by name) and builds mappings for all scripts.
     *
     * @param before the before program
     * @param after  the after program
     * @return a map from before block IDs to after block IDs, or empty if programs differ structurally
     */
    public static Map<String, String> buildMapping(Program before, Program after) {
        Map<String, String> mapping = new HashMap<>();

        for (ActorDefinition beforeActor : before.getActorDefinitionList().getDefinitions()) {
            String actorName = beforeActor.getIdent().getName();
            Optional<ActorDefinition> afterActorOpt = findActorByName(after, actorName);

            if (afterActorOpt.isPresent()) {
                Map<String, String> actorMapping = buildActorMapping(beforeActor, afterActorOpt.get());
                mapping.putAll(actorMapping);
            }
        }

        return mapping;
    }

    /**
     * Builds a block ID mapping between two actors.
     *
     * @param before the before actor
     * @param after  the after actor
     * @return a map from before block IDs to after block IDs
     */
    public static Map<String, String> buildActorMapping(ActorDefinition before, ActorDefinition after) {
        Map<String, String> mapping = new HashMap<>();

        // Map scripts
        List<Script> beforeScripts = before.getScripts().getScriptList();
        List<Script> afterScripts = after.getScripts().getScriptList();
        buildScriptListMapping(beforeScripts, afterScripts, mapping);

        // Map procedures
        List<ProcedureDefinition> beforeProcs = before.getProcedureDefinitionList().getList();
        List<ProcedureDefinition> afterProcs = after.getProcedureDefinitionList().getList();
        buildProcedureListMapping(beforeProcs, afterProcs, mapping);

        return mapping;
    }

    private static void buildScriptListMapping(
            List<Script> beforeScripts,
            List<Script> afterScripts,
            Map<String, String> mapping
    ) {
        Set<Script> matchedAfter = new HashSet<>();

        for (Script beforeScript : beforeScripts) {
            for (Script afterScript : afterScripts) {
                if (matchedAfter.contains(afterScript)) {
                    continue;
                }

                if (StructuralEquality.areStructurallyEqual(beforeScript, afterScript)) {
                    collectBlockIdMappings(beforeScript, afterScript, mapping);
                    matchedAfter.add(afterScript);
                    break;
                }
            }
        }
    }

    private static void buildProcedureListMapping(
            List<ProcedureDefinition> beforeProcs,
            List<ProcedureDefinition> afterProcs,
            Map<String, String> mapping
    ) {
        Set<ProcedureDefinition> matchedAfter = new HashSet<>();

        for (ProcedureDefinition beforeProc : beforeProcs) {
            for (ProcedureDefinition afterProc : afterProcs) {
                if (matchedAfter.contains(afterProc)) {
                    continue;
                }

                if (StructuralEquality.areStructurallyEqual(beforeProc, afterProc)) {
                    collectBlockIdMappings(beforeProc, afterProc, mapping);
                    matchedAfter.add(afterProc);
                    break;
                }
            }
        }
    }

    /**
     * Collects block ID mappings by parallel traversal of two structurally equal nodes.
     *
     * <p>Assumes the nodes are already verified to be structurally equal.
     *
     * @param before  the before node
     * @param after   the after node
     * @param mapping the map to populate with block ID correspondences
     */
    private static void collectBlockIdMappings(ASTNode before, ASTNode after, Map<String, String> mapping) {
        String beforeId = AstNodeUtil.getBlockId(before);
        String afterId = AstNodeUtil.getBlockId(after);

        if (beforeId != null && !beforeId.isBlank() && afterId != null && !afterId.isBlank()) {
            mapping.put(beforeId, afterId);
        }

        // Recursively process children (excluding metadata nodes)
        var beforeChildren = before.getChildren().stream()
                .filter(Predicate.not(AstNodeUtil::isMetadata))
                .toList();
        var afterChildren = after.getChildren().stream()
                .filter(Predicate.not(AstNodeUtil::isMetadata))
                .toList();

        // Since nodes are structurally equal, they have the same number of children
        for (int i = 0; i < beforeChildren.size(); i++) {
            collectBlockIdMappings(beforeChildren.get(i), afterChildren.get(i), mapping);
        }
    }

    private static Optional<ActorDefinition> findActorByName(Program program, String name) {
        return program.getActorDefinitionList().getDefinitions().stream()
                .filter(a -> a.getIdent().getName().equals(name))
                .findFirst();
    }
}

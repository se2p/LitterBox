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
package de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow;

import de.uni_passau.fim.se2.litterbox.analytics.bugs.Type;
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.dataflow.JoinFunction;

import java.util.*;

public class TypeJoinFunction implements JoinFunction<VariableTypeFact> {

    @Override
    public Set<VariableTypeFact> apply(Set<Set<VariableTypeFact>> inFacts) {
        if (inFacts.isEmpty()) {
            return Collections.emptySet();
        }

        // Map variable to joined Type
        Map<Defineable, Type> joinedTypes = new LinkedHashMap<>();
        
        // Track which variables we have seen to handle BOTTOM (absence)
        // Actually, May analysis usually means if it exists in ONE path, it exists in result?
        // Wait, Reaching Definitions: Union of all definitions.
        // If one path defines X, and other doesn't, result has X? Yes.
        // But here we track "Type of X".
        // If one path has X=NUM, other has X=Uninit(BOTTOM).
        // Join(NUM, BOTTOM) = NUM.
        // So we just iterate all facts and merge.
        
        for (Set<VariableTypeFact> factSet : inFacts) {
            for (VariableTypeFact fact : factSet) {
                Defineable var = fact.getVariable();
                Type type = fact.getType();
                
                joinedTypes.merge(var, type, this::mergeTypes);
            }
        }
        
        Set<VariableTypeFact> result = new LinkedHashSet<>();
        for (Map.Entry<Defineable, Type> entry : joinedTypes.entrySet()) {
            result.add(new VariableTypeFact(entry.getKey(), entry.getValue()));
        }
        
        return result;
    }

    private Type mergeTypes(Type t1, Type t2) {
        if (t1 == t2) return t1;
        if (t1 == Type.TOP || t2 == Type.TOP) return Type.TOP;
        if (t1 == Type.ANY || t2 == Type.ANY) return Type.ANY; // Wildcard dominates specific types (safe assumption)
        return Type.TOP; // Conflict (e.g. NUMBER vs STRING)
    }
}

/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.ast.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ScriptReplacementVisitor extends OnlyCodeCloneVisitor {

    private final Script target;
    private final Set<Script> otherTargets = Sets.newIdentityHashSet();
    private final List<Script> replacementScripts;

    public ScriptReplacementVisitor(Script target, List<Script> replacement) {
        this.target = target;
        this.replacementScripts = replacement;
    }

    public ScriptReplacementVisitor(Script target, Script... replacement) {
        this.target = target;
        this.replacementScripts = Arrays.asList(replacement);
    }

    public ScriptReplacementVisitor(Script target, List<Script> otherTargets, List<Script> replacement) {
        this.target = target;
        this.otherTargets.addAll(otherTargets);
        this.replacementScripts = replacement;
    }

    protected boolean isTargetScript(ScriptEntity node) {
        return node == target;
    }


    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script script : node.getScriptList()) {
            if (isTargetScript(script)) {
                scripts.addAll(replacementScripts);
            } else if (!otherTargets.contains(script)) {
                scripts.add(apply(script));
            }
        }
        return new ScriptList(scripts);
    }
}

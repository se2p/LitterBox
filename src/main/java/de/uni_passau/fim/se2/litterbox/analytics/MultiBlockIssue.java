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
package de.uni_passau.fim.se2.litterbox.analytics;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.NormalizationVisitor;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;

public class MultiBlockIssue extends Issue {

    private final List<ASTNode> nodes = new ArrayList<>();
    private final List<ScriptEntity> scripts = new ArrayList<>();

    private List<ASTNode> normalizedNodes;

    public MultiBlockIssue(IssueFinder finder, IssueSeverity severity, Program program, ActorDefinition actor, ScriptEntity script, List<ASTNode> nodes, Metadata metaData, Hint hint) {
        super(finder, severity, program, actor, script, nodes.getFirst(), metaData, hint);
        this.nodes.addAll(nodes);
        scripts.add(script);
    }

    public MultiBlockIssue(IssueFinder finder, IssueSeverity severity, Program program, ActorDefinition actor, List<ScriptEntity> scripts, List<ASTNode> nodes, Metadata metaData, Hint hint) {
        super(finder, severity, program, actor, scripts.getFirst(), nodes.getFirst(), metaData, hint);
        this.nodes.addAll(nodes);
        this.scripts.addAll(scripts);
    }

    @Override
    public boolean isCodeLocation(ASTNode node) {
        // TODO: Workaround until we have figured out how to implement hashCode/equals properly
        Set<ASTNode> uniqueNodes = Sets.newIdentityHashSet();
        uniqueNodes.addAll(nodes);
        return uniqueNodes.contains(node);
    }

    @Override
    public boolean hasMultipleBlocks() {
        return nodes.size() > 1;
    }

    public List<ScriptEntity> getScriptEntities() {
        return Collections.unmodifiableList(scripts);
    }

    @Override
    public Script getScript() {
        return scripts.getFirst() instanceof Script script ? script : null;
    }

    @Override
    public ProcedureDefinition getProcedure() {
        return scripts.getFirst() instanceof ProcedureDefinition procedure ? procedure : null;
    }

    @Override
    public ScriptEntity getScriptOrProcedureDefinition() {
        return scripts.getFirst();
    }

    public List<ScriptEntity> getScriptsOrProcedureDefinitions() {
        return scripts;
    }

    public List<ASTNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<ASTNode> getNormalizedNodes() {
        if (normalizedNodes == null) {
            NormalizationVisitor normalizationVisitor = new NormalizationVisitor();
            normalizedNodes = nodes.stream().map(normalizationVisitor::apply).toList();
        }
        return normalizedNodes;
    }
}

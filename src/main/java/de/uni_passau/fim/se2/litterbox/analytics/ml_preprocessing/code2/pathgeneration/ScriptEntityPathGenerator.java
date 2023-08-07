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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.visitor.ExtractScriptLeavesVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;
import java.util.stream.Stream;

public final class ScriptEntityPathGenerator extends PathGenerator {

    private final Map<ScriptEntity, List<ASTNode>> leavesMap;

    public ScriptEntityPathGenerator(
            Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites,
            PathFormatOptions pathFormatOptions
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions);

        List<ActorDefinition> sprites = AstNodeUtil.getActors(program, includeStage);
        this.leavesMap = Collections.unmodifiableMap(extractASTLeaves(sprites));
    }

    private Map<ScriptEntity, List<ASTNode>> extractASTLeaves(List<ActorDefinition> sprites) {
        ExtractScriptLeavesVisitor extractionVisitor = new ExtractScriptLeavesVisitor(program.getProcedureMapping());

        for (ActorDefinition sprite : sprites) {
            for (Script script : sprite.getScripts().getScriptList()) {
                script.accept(extractionVisitor);
            }

            for (ProcedureDefinition procedure : sprite.getProcedureDefinitionList().getList()) {
                procedure.accept(extractionVisitor);
            }
        }

        return extractionVisitor.getLeaves();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        final List<ProgramFeatures> scriptFeatures = new ArrayList<>();

        for (final var entry : leavesMap.entrySet()) {
            final ScriptEntity script = entry.getKey();

            NodeNameUtil.getScriptEntityName(script).ifPresent(scriptName -> {
                final List<ASTNode> leaves = entry.getValue();
                final ProgramFeatures singleScriptFeatures = super.getProgramFeatures(scriptName, leaves);

                if (!singleScriptFeatures.isEmpty()) {
                    scriptFeatures.add(singleScriptFeatures);
                }
            });
        }

        return scriptFeatures;
    }

    @Override
    public Stream<ASTNode> getLeaves() {
        return leavesMap.values().stream().flatMap(Collection::stream);
    }
}

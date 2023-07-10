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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.visitor.ExtractScriptLeavesVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;

public final class ScriptEntityPathGenerator extends PathGenerator {

    private final Map<ScriptEntity, List<ASTNode>> leafsMap;

    public ScriptEntityPathGenerator(
            Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites);

        List<ActorDefinition> sprites = AstNodeUtil.getActors(program, includeStage);
        this.leafsMap = Collections.unmodifiableMap(collectLeaves(sprites));
    }

    private Map<ScriptEntity, List<ASTNode>> collectLeaves(final List<ActorDefinition> sprites) {
        final Map<ScriptEntity, List<ASTNode>> leavesMap = new HashMap<>();

        leavesMap.putAll(extractScriptsASTLeafs(sprites));
        leavesMap.putAll(extractProcedureDefinitionsASTLeafs(sprites));

        return leavesMap;
    }

    private Map<ScriptEntity, List<ASTNode>> extractScriptsASTLeafs(List<ActorDefinition> sprites) {
        ExtractScriptLeavesVisitor extractionVisitor = new ExtractScriptLeavesVisitor();

        for (ActorDefinition sprite : sprites) {
            for (Script script : sprite.getScripts().getScriptList()) {
                script.accept(extractionVisitor);
            }
        }

        return extractionVisitor.getLeavesMap();
    }

    private Map<ScriptEntity, List<ASTNode>> extractProcedureDefinitionsASTLeafs(List<ActorDefinition> sprites) {
        ExtractScriptLeavesVisitor extractionVisitor = new ExtractScriptLeavesVisitor();

        for (ActorDefinition sprite : sprites) {
            for (ProcedureDefinition procedure : sprite.getProcedureDefinitionList().getList()) {
                procedure.accept(extractionVisitor);
            }
        }

        return extractionVisitor.getLeavesMap();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        List<ProgramFeatures> scriptFeatures = new ArrayList<>();
        leafsMap.forEach((script, leafs) -> {
            var scriptName = NodeNameUtil.getScriptEntityName(script);
            if (scriptName.isPresent()) {
                ProgramFeatures singleScriptFeatures = super.getProgramFeatures(String.valueOf(scriptName), leafs);
                if (isValidateScriptFeature(singleScriptFeatures)) {
                    scriptFeatures.add(singleScriptFeatures);
                }
            }
        });
        return scriptFeatures;
    }

    private boolean isValidateScriptFeature(ProgramFeatures singleScriptFeatures) {
        return !singleScriptFeatures.isEmpty();
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(TokenVisitorFactory::getNormalisedToken)
                .toList();
    }
}

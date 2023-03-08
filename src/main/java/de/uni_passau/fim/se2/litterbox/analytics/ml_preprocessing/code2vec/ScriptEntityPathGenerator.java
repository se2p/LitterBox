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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractProcedureDefinitionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractScriptVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ScriptEntityPathGenerator extends PathGenerator {

    private static final Logger log = Logger.getLogger(ScriptEntityPathGenerator.class.getName());
    private final Map<ScriptEntity, List<ASTNode>> leafsMap;

    public ScriptEntityPathGenerator(Program program, int maxPathLength, boolean includeStage) {
        super(program, maxPathLength, includeStage);
        List<ActorDefinition> sprites = AstNodeUtil.getActors(program, includeStage);
        Map<ScriptEntity, List<ASTNode>> tmp = new HashMap<>();
        tmp.putAll(extractScriptsASTLeafs(sprites));
        tmp.putAll(extractProcedureDefinitionsASTLeafs(sprites));
        this.leafsMap = Collections.unmodifiableMap(tmp);
    }

    private Map<Script, List<ASTNode>> extractScriptsASTLeafs(List<ActorDefinition> sprites) {
        ExtractScriptVisitor scriptVisitor = new ExtractScriptVisitor();
        sprites.forEach(sprite -> sprite.getScripts().getScriptList().forEach(script -> script.accept(scriptVisitor)));
        return scriptVisitor.getLeafsMap();
    }

    private Map<ProcedureDefinition, List<ASTNode>> extractProcedureDefinitionsASTLeafs(List<ActorDefinition> sprites) {
        ExtractProcedureDefinitionVisitor extractProcedureDefinitionVisitor = new ExtractProcedureDefinitionVisitor();
        sprites.forEach(sprite -> sprite.getProcedureDefinitionList().getList().
                forEach(procedureDefinition -> procedureDefinition.accept(extractProcedureDefinitionVisitor)));
        return extractProcedureDefinitionVisitor.getLeafsMap();
    }

    @Override
    public void printLeafs() {
        System.out.println("Number of scripts: " + leafsMap.keySet().size());
        leafsMap.forEach((script, leafs) -> {
            System.out.println("Number of ASTLeafs for ScriptEntity " +
                    NodeNameUtils.getSpriteOrProcedureDefinitionName(script) + ": " + leafs.size());
            leafs.forEach(leaf -> {
                System.out.println(leafs.indexOf(leaf) + " Leaf (Test): " + StringUtil.getToken(leaf));
            });
        });
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        List<ProgramFeatures> scriptFeatures = new ArrayList<>();
        leafsMap.forEach((script, leafs) -> {
            String scriptName = NodeNameUtils.getSpriteOrProcedureDefinitionName(script);
            ProgramFeatures singleScriptFeatures = super.getProgramFeatures(scriptName, leafs);
            if (isValidateScriptFeature(scriptName, singleScriptFeatures, script)) {
                scriptFeatures.add(singleScriptFeatures);
            }
        });
        return scriptFeatures;
    }

    private boolean isValidateScriptFeature(String scriptName, ProgramFeatures singleScriptFeatures, ScriptEntity script) {
        if (scriptName == null) {
            log.severe("can't name for script with Id " + script.toString()); // TODO replace to string
            return false;
        }
        if (singleScriptFeatures.isEmpty()) {
            log.severe("can't generate paths for script with Id - empty features " + scriptName);
            return false;
        }
        return true;
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(StringUtil::getToken).collect(Collectors.toList());
    }
}

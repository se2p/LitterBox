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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractScriptVisitor;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptPathGenerator extends PathGenerator {

    private Map<Script, List<ASTNode>> leafsMap;

    public ScriptPathGenerator(int maxPathLength, boolean includeStage, Program program) {
        super(maxPathLength, includeStage,  program);
        extractASTLeafs();
    }

    @Override
    protected void extractASTLeafs() {
        ExtractScriptVisitor scriptVisitor = new ExtractScriptVisitor();
        List<ActorDefinition> sprites = AstNodeUtil.getActors(program, includeStage);
        sprites.forEach(sprite -> sprite.getScripts().getScriptList().
                        forEach(script -> script.accept(scriptVisitor)));
        leafsMap = scriptVisitor.getLeafsMap();
    }

    @Override
    public void printLeafs() {
        System.out.println("Number of scripts: " + leafsMap.keySet().size());
        for (Map.Entry<Script, List<ASTNode>> entry : leafsMap.entrySet()) {
            System.out.println("Number of ASTLeafs for Script " + generateScriptName(entry.getKey()) + ": " + entry.getValue().size());
            int i = 0;
            for (ASTNode value : entry.getValue()) {
                System.out.println(i + " Leaf (Test): " + StringUtil.getToken(value));
                i++;
            }
        }
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        List<ProgramFeatures> scriptFeatures = new ArrayList<>();
        for (Map.Entry<Script, List<ASTNode>> entry : leafsMap.entrySet()) {
            Script script = entry.getKey();
            List<ASTNode> leafs = entry.getValue();
            ProgramFeatures singleScriptFeatures = generatePathsForScript(script, leafs);
            if (singleScriptFeatures != null && !singleScriptFeatures.isEmpty()) {
                scriptFeatures.add(singleScriptFeatures);
            }
        }
        return scriptFeatures;
    }

    private ProgramFeatures generatePathsForScript(final Script script, final List<ASTNode> leafs) {
        return getProgramFeatures(generateScriptName(script), leafs);
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(StringUtil::getToken)
                .collect(Collectors.toList());
    }

    private String generateScriptName(Script script){
        ActorDefinition parentSprite = AstNodeUtil.findActor(script).get();
        return "spriteName_" + normalizeSpriteName(parentSprite.getIdent().getName()) + "_scriptId_" +  parentSprite.getScripts().getScriptList().indexOf(script);
    }
}

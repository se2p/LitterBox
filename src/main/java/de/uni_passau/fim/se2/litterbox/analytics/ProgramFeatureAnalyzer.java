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

import de.uni_passau.fim.se2.litterbox.analytics.metric.*;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProgramFeatureAnalyzer implements ProgramAnalyzer<List<FeatureResult>> {
    private final List<MetricExtractor<ASTNode>> metrics = Arrays.asList(
            new AvgBlockStatementCount<>(),
            new AvgScriptWidthCount<>(),
            new AvgVariableLengthCount<>(),
            new BlockCount<>(),
            new ControlBlockCount<>(),
            new EventsBlockCount<>(),
            new HalsteadVolume<>(),
            new HalsteadLength<>(),
            new HalsteadVocabulary<>(),
            new HalsteadDifficulty<>(),
            new HalsteadEffort<>(),
            new LooksBlockCount<>(),
            new MaxBlockStatementCount<>(),
            new MaxScriptWidthCount<>(),
            new MaxVariableLengthCount<>(),
            new MotionBlockCount<>(),
            new MyBlocksBlockCount<>(),
            new NestedBlockCount<>(),
            new OperatorsBlockCount<>(),
            new SensingBlockCount<>(),
            new SliceCoverage<>(),
            new SliceOverlap<>(),
            new SliceTightness<>(),
            new SoundBlockCount<>(),
            new StackedStatementCount<>(),
            new StatementCount<>(),
            new TokenEntropy<>(),
            new VariableCount<>(),
            new VariablesBlockCount<>(),
            new WeightedMethodCount<>()
    );

    @Override
    public List<FeatureResult> analyze(Program program) {
        return calculateFeatures(program);
    }

    public List<FeatureResult> calculateFeatures(Program program) {
        int actorCount = 0;
        List<ActorDefinition> actorDefinitions = AstNodeUtil.getActors(program, true).toList();
        List<FeatureResult> results = new ArrayList<>();
        for (ActorDefinition actorDefinition : actorDefinitions) {
            int scriptCount = 0;
            int procedureDefCount = 0;
            actorCount = actorCount + 1;
            List<ASTNode> targets = new ArrayList<>();
            targets.addAll(actorDefinition.getScripts().getScriptList());
            targets.addAll(actorDefinition.getProcedureDefinitionList().getList());

            for (ASTNode target : targets) {
                String uniqueId = "";
                if (target instanceof Script) {
                    scriptCount = scriptCount + 1;
                    uniqueId = "ACTOR" + actorCount + "_" + "SCRIPT" + scriptCount;
                } else if (target instanceof ProcedureDefinition) {
                    procedureDefCount = procedureDefCount + 1;
                    uniqueId = "ACTOR" + actorCount + "_" + "PROCEDUREDEFINITION" + procedureDefCount;
                }

                for (MetricExtractor<ASTNode> extractor : metrics) {
                    double metricResult = extractor.calculateMetric(target);
                    results.add(new FeatureResult(extractor.getName(), uniqueId, metricResult));
                }
            }
        }
        return results;
    }

    public List<MetricExtractor<ASTNode>> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }
}

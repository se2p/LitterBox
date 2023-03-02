package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProgramPathGenerator extends PathGenerator {

    private Map<ActorDefinition, List<ASTNode>> leafsMap;

    public ProgramPathGenerator(int maxPathLength, boolean includeStage, Program program) {
        super(maxPathLength, includeStage, program);
    }

    @Override
    public void printLeafs() {
        //
    }

    @Override
    public void extractASTLeafs() {
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(includeStage);
        program.accept(spriteVisitor);
        leafsMap = spriteVisitor.getLeafsCollector();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        return generatePathsWholeProgram().stream().collect(Collectors.toList());
    }

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        List<ASTNode> leafs = leafsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        final ProgramFeatures programFeatures = getProgramFeatures("program", leafs);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(StringUtil::getToken)
                .collect(Collectors.toList());
    }
}

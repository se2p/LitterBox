package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.*;
import java.util.stream.Collectors;

public final class ProgramPathGenerator extends PathGenerator {

    private final Map<ActorDefinition, List<ASTNode>> leafsMap;

    public ProgramPathGenerator(Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites) {
        super(program, maxPathLength, includeStage,includeDefaultSprites);
        this.leafsMap = Collections.unmodifiableMap(extractASTLeafs());
    }

    @Override
    public void printLeafs() {
        //
    }

    private Map<ActorDefinition, List<ASTNode>> extractASTLeafs() {
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(includeStage);
        program.accept(spriteVisitor);
        return spriteVisitor.getLeafsCollector();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        return generatePathsWholeProgram().stream().collect(Collectors.toList());
    }

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        List<ASTNode> leafs = leafsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        final ProgramFeatures programFeatures = super.getProgramFeatures("program", leafs);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(TokenVisitor::getNormalisedToken)
                .collect(Collectors.toList());
    }
}

package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteAndStageVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class PathGenerator {

    private final int maxPathLength;
    private final int maxPathWidth;
    private final boolean isStageIncluded;
    Program program;
    Map<ASTNode, List<ASTNode>> leafsMap;


    public PathGenerator(Program program, int maxPathLength, int maxPathWidth, boolean isStageIncluded){
        this.maxPathLength = maxPathLength;
        this.maxPathWidth = maxPathWidth;
        this.isStageIncluded=isStageIncluded;
        this.program = program;
    }

    public void extractASTLeafsPerSprite() {
    	if(isStageIncluded) {
    		ExtractSpriteAndStageVisitor spriteVisitor = new ExtractSpriteAndStageVisitor();
            program.accept(spriteVisitor);
            leafsMap = spriteVisitor.getLeafsCollector();
    	}
    	else {
    		ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor();
            program.accept(spriteVisitor);
            leafsMap = spriteVisitor.getLeafsCollector();
    	}    	
    }

    public void printLeafsPerSpite() {
        System.out.println("Anzahl der Sprites: " + leafsMap.keySet().size());
        for(ASTNode key : leafsMap.keySet()) {
            System.out.println("Actor Definition: " + ((ActorDefinition)key).getIdent().getName());
            System.out.println("Anzahl an ASTLeafs für " + ((ActorDefinition)key).getIdent().getName() + ": " + leafsMap.get(key).size());
            int i=0;
            for(ASTNode value : leafsMap.get(key)){
                System.out.println(i + "Blatt (Test): " + value.getUniqueName());
                i++;
            }
        }
    }

    public ArrayList<ProgramFeatures> generatePaths() {
        ArrayList<ProgramFeatures> spriteFeatures = new ArrayList<>();
        for(ASTNode sprite : leafsMap.keySet()) {
            ProgramFeatures singleSpriteFeatures = generatePathsPerSprite(sprite);
            if (!singleSpriteFeatures.isEmpty()) {
                spriteFeatures.add(singleSpriteFeatures);
            }
        }
        return spriteFeatures;
    }

    private ProgramFeatures generatePathsPerSprite(ASTNode sprite) {
        List<ASTNode> spriteLeafs = leafsMap.get(sprite);
        String spriteName = ((ActorDefinition)sprite).getIdent().getName();
        //Normalize SpriteLabel
        String normalizedSpriteLabel = StringUtil.normalizeName(spriteName, "BLANK");
        ArrayList<String> splitNameParts = StringUtil.splitToSubtokens(spriteName);
        String splitName = normalizedSpriteLabel;
        if (splitNameParts.size() > 0) {
            splitName = splitNameParts.stream().collect(Collectors.joining("|"));
        }

        ProgramFeatures programFeatures = new ProgramFeatures(splitName);

        for(int i = 0; i < spriteLeafs.size(); i++) {
            for(int j = i + 1; j < spriteLeafs.size(); j++) {
                String path = generatePath(spriteLeafs.get(i), spriteLeafs.get(j));
                if (path != ""){
                    String source = spriteLeafs.get(i).getUniqueName();
                    String target = spriteLeafs.get(j).getUniqueName();
                    programFeatures.addFeature(source, path, target);
                }
            }
        }
        return programFeatures;
    }

    private static ArrayList<ASTNode> getTreeStack(ASTNode node) {
        ArrayList<ASTNode> upStack = new ArrayList<>();
        ASTNode current = node;
        while (current != null) {
            upStack.add(current);
            current = current.getParentNode();
        }
        return upStack;
    }

    private String generatePath(ASTNode source, ASTNode target) {
        String down = "_";
        String up = "^";
        String startSymbol = "(";
        String endSymbol = ")";
        String seperator = "";

        StringJoiner stringBuilder = new StringJoiner(seperator);
        ArrayList<ASTNode> sourceStack = getTreeStack(source);
        ArrayList<ASTNode> targetStack = getTreeStack(target);

        int commonPrefix = 0;
        int currentSourceAncestorIndex = sourceStack.size() - 1;
        int currentTargetAncestorIndex = targetStack.size() - 1;

        while (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0
                && sourceStack.get(currentSourceAncestorIndex) == targetStack.get(currentTargetAncestorIndex)) {
            commonPrefix++;
            currentSourceAncestorIndex--;
            currentTargetAncestorIndex--;
        }

        //manage too long path length
        int pathLength = sourceStack.size() + targetStack.size() - 2 * commonPrefix;
        if (maxPathLength > 0) {
            if (pathLength > maxPathLength) {
                return "";
            }
        }


        /*
        //manage path width
        if (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0) {
            int pathWidth = targetStack.get(currentTargetAncestorIndex).getUserData(Common.ChildId)
                    - sourceStack.get(currentSourceAncestorIndex).getUserData(Common.ChildId);
            if (pathWidth > maxPathWidth) {
                return "";
            }
        }
        */

        //add source Stack Nodes until common Node(up to the common)
        for (int i = 0; i < sourceStack.size() - commonPrefix; i++) {
            ASTNode currentNode = sourceStack.get(i);
            String childId = "";
            stringBuilder.add(String.format("%s%s%s%s%s", startSymbol,
                    currentNode.getUniqueName(), childId, endSymbol, up));
        }

        //add common Node
        ASTNode commonNode = sourceStack.get(sourceStack.size() - commonPrefix);
        String commonNodeChildId = "";
        stringBuilder.add(String.format("%s%s%s%s", startSymbol,
                commonNode.getUniqueName(), commonNodeChildId, endSymbol));

        //add target Stack Nodes (down to the target)
        for (int i = targetStack.size() - commonPrefix - 1; i >= 0; i--) {
            ASTNode currentNode = targetStack.get(i);
            String childId = "";
            stringBuilder.add(String.format("%s%s%s%s%s", down, startSymbol,
                    currentNode.getUniqueName(), childId, endSymbol));
        }

        return stringBuilder.toString();
    }



}

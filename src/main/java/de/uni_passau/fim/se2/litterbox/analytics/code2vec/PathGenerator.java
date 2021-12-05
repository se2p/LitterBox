package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class PathGenerator {

    private final int maxPathLength;
    Program program;
    Map<ASTNode, List<ASTNode>> leafsMap;

    private static final String[] SPRITE_LANGUAGES = {"Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš",
            "Hahmo", "Kihusika", "Kukla", "Lik", "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia",
            "Postava", "Pêlîstik", "Sprait", "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning",
            "Umlingisi", "Veikėjas", "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن",
            "تەن", "شکلک", "สไปรต์", "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"};


    public PathGenerator(Program program, int maxPathLength){
        this.maxPathLength = maxPathLength;
        this.program = program;
        extractASTLeafsPerSprite();
    }

    private void extractASTLeafsPerSprite() {
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor();
        program.accept(spriteVisitor);
        leafsMap = spriteVisitor.getLeafsCollector();
    }

    public void printLeafsPerSprite() {
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
            if (singleSpriteFeatures != null && !singleSpriteFeatures.isEmpty()) {
                spriteFeatures.add(singleSpriteFeatures);
            }
        }
        return spriteFeatures;
    }

    private ProgramFeatures generatePathsPerSprite(ASTNode sprite) {
        List<ASTNode> spriteLeafs = leafsMap.get(sprite);
        String spriteName = ((ActorDefinition)sprite).getIdent().getName();
        //Normalize SpriteLabel
        String normalizedSpriteLabel = StringUtil.normalizeName(spriteName);
        if (isDefaultName(normalizedSpriteLabel) || normalizedSpriteLabel.isEmpty()) {
            return null;
        }
        ArrayList<String> splitNameParts = StringUtil.splitToSubtokens(spriteName);
        String splitName = normalizedSpriteLabel;
        if (splitNameParts.size() > 0) {
            splitName = String.join("|", splitNameParts);
        }

        ProgramFeatures programFeatures = new ProgramFeatures(splitName);

        for(int i = 0; i < spriteLeafs.size(); i++) {
            for(int j = i + 1; j < spriteLeafs.size(); j++) {
                String path = generatePath(spriteLeafs.get(i), spriteLeafs.get(j));
                if (!path.equals("")){
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

    private boolean isDefaultName(String normalizedSpriteLabel) {
        boolean isDefaultName = false;
        for (String defaultName : SPRITE_LANGUAGES) {
            if (defaultName.toLowerCase().equals(normalizedSpriteLabel)){
                isDefaultName = true;
                break;
            }
        }
        return isDefaultName;
    }

}

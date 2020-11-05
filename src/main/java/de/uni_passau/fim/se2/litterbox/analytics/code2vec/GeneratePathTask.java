package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GeneratePathTask {

    private final Program program;
    private final int maxPathLength;
    private final int maxPathWidth;


    public GeneratePathTask(Program program, int maxPathLength, int maxPathWidth){
        this.program = program;
        this.maxPathLength = maxPathLength;
        this.maxPathWidth = maxPathWidth;
        createContextForCode2Vec();
    }

    private void createContextForCode2Vec() {
        PathGenerator pathGenerator = new PathGenerator(program, maxPathLength, maxPathWidth);
        pathGenerator.extractASTLeafsPerSprite();
        //pathGenerator.printLeafsPerSpite();
        ArrayList<ProgramFeatures> programs = pathGenerator.generatePaths();
        String toPrint = featuresToString(programs);
        if (toPrint.length() > 0) {
            System.out.println(toPrint);
        }
    }

    private String featuresToString(ArrayList<ProgramFeatures> features) {
        if (features == null || features.isEmpty()) {
            return "";
        }

        List<String> spriteOutputs = new ArrayList<>();

        for (ProgramFeatures singleSpriteFeatures : features) {
            StringBuilder builder = new StringBuilder();

            String toPrint = "";
            toPrint = singleSpriteFeatures.toString();
            builder.append(toPrint);

            spriteOutputs.add(builder.toString());
        }
        // Anzahl an triples = (leafs * (leafs - 1)) / 2
        //System.out.println("Anzahl an Tripels: " + features.get(0).getFeatures().size());

        return StringUtils.join(spriteOutputs, "\n");
    }
}

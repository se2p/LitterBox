package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GeneratePathTask {

    private final Program program;
    private final int maxPathLength;


    public GeneratePathTask(Program program, int maxPathLength){
        this.program = program;
        this.maxPathLength = maxPathLength;
    }

    public String createContextForCode2Vec() {
        PathGenerator pathGenerator = new PathGenerator(program, maxPathLength);
        pathGenerator.extractASTLeafsPerSprite();
        //pathGenerator.printLeafsPerSpite();
        ArrayList<ProgramFeatures> programs = pathGenerator.generatePaths();
        return featuresToString(programs);
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

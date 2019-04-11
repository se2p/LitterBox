import clone.detection.ComparisonAlgorithm;
import clone.detection.Formatting;
import clone.detection.Normalization;
import clone.detection.Preparation;
import clone.structure.CloneBlock;
import clone.structure.ClonePairCode;
import scratch2.structure.Project;
import utils.JsonParser;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * This class controls the clone detection and contains the main method.
 */
public class CloneDetection {

	// The folder where the to analyze code is.
    private final static File folder = new File("C:\\Users\\magge\\Desktop\\Uni\\6. Semester\\Bachelorarbeit\\ScratchTest\\TestEinlesen\\test");

    /**
     * The main method reads the json file from the scratch project and runs 
     * the clone detection algorithm.
     */
    public static void main(String[] args) {
        try {
            Project project = null;
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println(fileEntry.getName());
                    try {
                        project = JsonParser.parse(fileEntry.getName(), fileEntry.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert project != null;
                    Preparation preparation = new Preparation();
                    
                    // This is the original code separated in big blocks.
                    List<String> preparatedCode = preparation.codePreparation(project);
                    Normalization norm = new Normalization();
                    
                    // This is the code who is normalized.
                    List<String> normalizedCode = norm.codeNormalization(preparatedCode);
                    ComparisonAlgorithm compare = new ComparisonAlgorithm(normalizedCode);
                    
                    // These are the blocks who are cloned saved as int tuples.
                    List<CloneBlock> blocks = compare.findClones();
                    Formatting formatting = new Formatting();
                    
                    // These are the blocks who are cloned saved as String tuples.
                    List<ClonePairCode> formattedCode = formatting.formatting(blocks, preparatedCode);
                    int numberOfClones = formattedCode.size();
                    StringBuilder sb = new StringBuilder();
                    for(ClonePairCode pair : formattedCode) {
                    	sb.append("The code:");
                    	sb.append(System.getProperty("line.separator"));
                    	for(String s : pair.getBlockOne()) {
                    		sb.append(s);
                    		sb.append(System.getProperty("line.separator"));
                    	}
                    	sb.append(System.getProperty("line.separator"));
                    	sb.append("is a clone of: ");
                    	sb.append(System.getProperty("line.separator"));
                    	for(String s : pair.getBlockTwo()) {
                    		sb.append(s);
                    		sb.append(System.getProperty("line.separator"));
                    	}
                    	sb.append("------------");
                    	sb.append(System.getProperty("line.separator"));
                    }
                    System.out.println("------------");
                    System.out.println("Number of clones: " + numberOfClones);
                    System.out.println("------------");
                    System.out.println(sb.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

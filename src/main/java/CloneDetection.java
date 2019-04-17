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
                    ComparisonAlgorithm compare = new ComparisonAlgorithm();
                    
                    // These are the blocks who are cloned saved as int tuples.
                    List<List<CloneBlock>> blocks = compare.findAllClones(normalizedCode);
                    Formatting formatting = new Formatting();
                    
                    // These are the blocks who are cloned saved as String tuples.
                    List<List<ClonePairCode>> formattedCode = formatting.formatting(blocks, preparatedCode);
                    int numberOfSprites = formattedCode.size() - 2;
                    StringBuilder sb = new StringBuilder();
                    for(ClonePairCode pair : formattedCode.get(0)) {
                        sb.append("Clones in Stagecode:");
                        sb.append(System.getProperty("line.separator"));
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
                    for(int i = 1; i <= numberOfSprites; i++) {
                    	sb.append("Clones in Sprite " + i);
                        for(ClonePairCode pair : formattedCode.get(i)) {
                            sb.append(System.getProperty("line.separator"));
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
                    }
                    sb.append(System.getProperty("line.separator"));
                    sb.append("Clones between Sprites:");
                    for(ClonePairCode pair : formattedCode.get(formattedCode.size() - 1)) {
                        sb.append(System.getProperty("line.separator"));
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
                    int numberOfClones = 0;
                    for(List<ClonePairCode> pair : formattedCode) {
                    	numberOfClones = numberOfClones + pair.size();
                    }
                    System.out.println("Number of Sprites: " + numberOfSprites);
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

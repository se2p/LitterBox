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
                    
                    // This is the original code separated in scripts.
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
                    
                    System.out.println("------------");
                    int numberOfClones = 0;
                    for(List<ClonePairCode> pair : formattedCode) {
                    	numberOfClones = numberOfClones + pair.size();
                    }
                    System.out.println("Number of Sprites: " + numberOfSprites);
                    System.out.println("Number of clones: " + numberOfClones);
                    System.out.println("------------");
                    listStageClones(formattedCode);
                    listSpriteClones(formattedCode, numberOfSprites);
                    listClonesBetweenSprites(formattedCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This main method runs the algorithm to detect clones in remixes and says
     * whether the clone is from the original project or is a new clone.
     */
    public static void mainremix(String[] args) {
    	try {
            Project project = null;
            Project remix = null;
            
            /*
             * The original project must be the first project in the folder and
             * the remix the second. 
             */
            final File fileEntry = Objects.requireNonNull(folder.listFiles())[0];
            final File fileEntryRemix = Objects.requireNonNull(folder.listFiles())[1];
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println("------------");
                    System.out.println("Projekt: " + fileEntry.getName());
                    System.out.println("Remix: " + fileEntryRemix.getName());
                    System.out.println("------------");
                    try {
                        project = JsonParser.parse(fileEntry.getName(), fileEntry.getPath());
                        remix = JsonParser.parse(fileEntryRemix.getName(), fileEntryRemix.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert project != null;
                    assert remix != null;
                    Preparation preparation = new Preparation();
                    
                    // The code is separated in scripts.
                    List<String> preparatedProject = preparation.codePreparation(project);
                    List<String> preparatedRemix = preparation.codePreparation(remix);
                    Normalization norm = new Normalization();
                    
                    // The code is normalized.
                    List<String> normalizedProject = norm.codeNormalization(preparatedProject);
                    List<String> normalizedRemix = norm.codeNormalization(preparatedRemix);
                    ComparisonAlgorithm comp = new ComparisonAlgorithm();
                    
                    /*
                     * These are the clones saved with the line number. 
                     * In the first list there are the clones between the projects.
                     * In the second list there are the clones from the original project.
                     * And in the third list there are the clones from the remix.
                     */
                    List<List<CloneBlock>> allClones = comp.findClonesRemix(normalizedProject, normalizedRemix);
                    Formatting form = new Formatting();
                    
                    // These are the clones saved as code.
                    List<List<ClonePairCode>> formattedCode = form.formattingRemix(allClones, preparatedProject, preparatedRemix);
                    int numberOfClones = 0;
                    for(List<ClonePairCode> pair : formattedCode) {
                    	numberOfClones = numberOfClones + pair.size();
                    }
                    int numberClonesBetweenProjects = formattedCode.get(0).size();
                    int numberClonesOriginal = formattedCode.get(1).size();
                    int numberClonesRemix = formattedCode.get(2).size();
                    System.out.println("Total number of clones: " + numberOfClones);
                    System.out.println("Number of clones between projects: " + numberClonesBetweenProjects);
                    System.out.println("Number of clones in original code: " + numberClonesOriginal);
                    System.out.println("Number of new clones in the remix: " + numberClonesRemix);
                    System.out.println("------------");
                    
                    // Prints the clones.
                    listClonesBetweenProjects(formattedCode);
                    listClonesOriginal(formattedCode);
                    listClonesRemix(formattedCode);
                }
    	} catch (Exception e) {
            e.printStackTrace();
        }
                
    }
    
    private static void listStageClones(List<List<ClonePairCode>> formattedCode) {
    	StringBuilder sb = new StringBuilder();
    	
    	// The stage clones are saved in the list at the index 0.
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
    	    System.out.println(sb.toString());
        }
    }
    
    private static void listSpriteClones(List<List<ClonePairCode>> formattedCode, int numberOfSprites) {
    	StringBuilder sb = new StringBuilder();
    	
    	// The clones in the sprites are saved in the list at the index 1 till list.length - 2.
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
    	System.out.println(sb.toString());
    }
    
    private static void listClonesBetweenSprites(List<List<ClonePairCode>> formattedCode) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(System.getProperty("line.separator"));
        sb.append("Clones between Sprites:");
        
        // The clones between sprites are saved at the last index in the list.
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
        System.out.println(sb.toString());
    }
    
    private static void listClonesBetweenProjects(List<List<ClonePairCode>> formattedCode) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(System.getProperty("line.separator"));
        sb.append("Clones between Projects:");
        
        // The clones between the project are saved in the list at the index 0.
        for(ClonePairCode pair : formattedCode.get(0)) {
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
        System.out.println(sb.toString());
    }
    
    private static void listClonesOriginal(List<List<ClonePairCode>> formattedCode) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(System.getProperty("line.separator"));
        sb.append("Clones in Original:");

        // The clones in the original project are saved in the list at the index 1.
        for(ClonePairCode pair : formattedCode.get(1)) {
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
        System.out.println(sb.toString());
    }
    
    private static void listClonesRemix(List<List<ClonePairCode>> formattedCode) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(System.getProperty("line.separator"));
        sb.append("New clones in Remix:");

        // The new clones in the remix are saved in the list at the index 2.
        for(ClonePairCode pair : formattedCode.get(2)) {
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
        System.out.println(sb.toString());
    }
}

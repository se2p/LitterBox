import clone.detection.ComparisonAlgorithm;
import clone.detection.Formatting;
import clone.detection.Normalization;
import clone.detection.Preparation;
import clone.structure.CloneBlock;
import clone.structure.ClonePairCode;
import scratch2.structure.Project;
import utils.CSVWriter;
import utils.JsonParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class controls the clone detection and contains the main method.
 */
public class CloneDetection {

	// The folder where the to analyze code is.
    private final static File folder = new File("...");
    
    /**
     * This main-method returns the total number of clones from a scratch 1 or 2 
     * projects.
     */
    public static void main(String[] args) {
    	try {
    		int numberFiles = folder.listFiles().length;
    		int[] numberClones = new int[numberFiles];
    		String[] projectName = new String[numberFiles];
    		for(int i = 0; i < numberFiles; i++) {
    			try {
    		    Project project = null;
                final File fileEntry = Objects.requireNonNull(folder.listFiles())[i];
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println("------------");
                    System.out.println("Projekt: " + fileEntry.getName());
                    System.out.println("------------");
                    try {
                        project = JsonParser.parse(fileEntry.getName(), fileEntry.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(project != null) {
                    	String name = project.getName();
                        Preparation preparation = new Preparation();
                        List<String> preparatedProject = preparation.codePreparation(project);
                        Normalization norm = new Normalization();
                        List<String> normalizedProject = norm.codeNormalization(preparatedProject);
                        ComparisonAlgorithm comp = new ComparisonAlgorithm();
                        List<List<CloneBlock>> allClones = comp.findAllClones(normalizedProject);
                        Formatting form = new Formatting();
                        List<List<ClonePairCode>> formattedCode = form.formatting(allClones, preparatedProject);
                        int numberOfClones = 0;
                        for(List<ClonePairCode> pair : formattedCode) {
                    	    numberOfClones = numberOfClones + pair.size();
                        }
                        numberClones[i] = numberOfClones;
                        projectName[i] = name;
                    }
                }
    			} catch(Exception e) {
    				continue;
    			}
    		}
    		String fileName = "clones.csv";
    		CSVWriter.writeCSVOnlyTotal(numberClones, projectName, fileName);
    	} catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This main-Method returns the total number of clones, the number of clones 
     * with gaps and the number of clones without gaps.
     */
    public static void mainGaps(String[] args) {
    	try {
    		int numberFiles = folder.listFiles().length;
    		int[] numberClonesWithoutGap = new int[numberFiles];
    		int[] numberClonesWithGap = new int[numberFiles];
    		String[] projectName = new String[numberFiles];
    		for(int i = 0; i < numberFiles; i++) {
    			try {
    		    Project project = null;
                final File fileEntry = Objects.requireNonNull(folder.listFiles())[i];
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println("------------");
                    System.out.println("Projekt: " + fileEntry.getName());
                    System.out.println("------------");
                    try {
                        project = JsonParser.parseRaw(fileEntry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(project != null) {
                    	String name = project.getName();
                        Preparation preparation = new Preparation();
                        List<String> preparatedProject = preparation.codePreparation(project);
                        Normalization norm = new Normalization();
                        List<String> normalizedProject = norm.codeNormalization(preparatedProject);
                        ComparisonAlgorithm comp = new ComparisonAlgorithm();
                        List<List<CloneBlock>> allClones = comp.findAllClonesGap(normalizedProject);
                        numberClonesWithoutGap[i] = allClones.get(0).size();
                        numberClonesWithGap[i] = allClones.get(1).size();
                        projectName[i] = name;
                    }
                }
    			} catch(Exception e) {
    				continue;
    			}
    		}
    		String fileName = "clonesGap.csv";
    		CSVWriter.writeCSVGap(numberClonesWithoutGap, numberClonesWithGap, fileName, projectName);
    	} catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This main method returns the total number of clones in the project,  
     * between sprites and in the sprites.
     */
    public static void mainWhere(String[] args) {
    	String name = "clones.csv";
    	List<List<List<ClonePairCode>>> allClones = new ArrayList<List<List<ClonePairCode>>>();
    	List<String> allNames = new ArrayList<String>();
        try {
            Project project = null;
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println(fileEntry.getName());
                    try {
                        project = JsonParser.parseRaw(fileEntry);
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
                    String projectName = project.getName();
                    allClones.add(formattedCode);
                    allNames.add(projectName);
                    
                }
            }
            CSVWriter.writeCSV(allClones, name, allNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This main method runs the algorithm to detect clones in remixes and says
     * whether the clone is from the original project or is a new clone.
     */
    public static void mainRemix(String[] args) {
    	
    	/* 
    	 * This is the number of remixes included the original which are written
    	 * in one csv-file.
    	 */
    	int numberOfProjectsInOneFile = 100;
    	try {
            Project project = null;
            Project remix = null;
            int numberFiles = folder.listFiles().length;
            String[] nameOriginal = new String[numberOfProjectsInOneFile];
            String[] nameRemix = new String[numberOfProjectsInOneFile];
            int[] clonesOriginal = new int[numberOfProjectsInOneFile];
            int[] clonesOnlyRemix = new int[numberOfProjectsInOneFile];
            int[] clonesTotal = new int[numberOfProjectsInOneFile];
            int[] clonesBetween = new int[numberOfProjectsInOneFile];
            /*
             * The original project must be the first project in the folder and
             * the remix the second. 
             */
            int count = 0;
            int fileNumber = 0;
            for(int i = 0; i < numberFiles; i += 2) {
            	count++;
                final File fileEntry = Objects.requireNonNull(folder.listFiles())[i];
                final File fileEntryRemix = Objects.requireNonNull(folder.listFiles())[i + 1];
                if (!fileEntry.isDirectory()) {
                    System.out.println("Projekt: " + fileEntry.getName());
                    System.out.println("Remix: " + fileEntryRemix.getName());
                    System.out.println("------------");
                    try {
                        project = JsonParser.parseRaw(fileEntry);
                        remix = JsonParser.parseRaw(fileEntryRemix);
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
                    char[] originalName = project.getName().toCharArray();
                    char[] remixName = remix.getName().toCharArray();
                    int indexShift = 0;
                    while(originalName[indexShift] != '_') {
                    	indexShift++;
                    }
                    char[] realNameO = Arrays.copyOfRange(originalName, indexShift + 1, originalName.length);
                    char[] realNameR = Arrays.copyOfRange(remixName, indexShift + 1, remixName.length);
                    String nameO = "";
                    String nameR = "";
                    for(char c : realNameO) {
                    	nameO += c;
                    }
                    for(char c : realNameR) {
                    	nameR += c;
                    }
                    nameOriginal[((i + 1) / 2) % numberOfProjectsInOneFile] = nameO;
                    nameRemix[((i + 1) / 2) % numberOfProjectsInOneFile] = nameR;
                    clonesOriginal[((i + 1) / 2) % numberOfProjectsInOneFile] = numberClonesOriginal;
                    clonesOnlyRemix[((i + 1) / 2) % numberOfProjectsInOneFile] = numberClonesRemix;
                    clonesTotal[((i + 1) / 2) % numberOfProjectsInOneFile] = numberOfClones;
                    clonesBetween[((i + 1) / 2) % numberOfProjectsInOneFile] = numberClonesBetweenProjects;
                }
                if(count != 0 && count % numberOfProjectsInOneFile == 0) {
                	fileNumber++;
                	CSVWriter.writeCSVRemix(nameOriginal, nameRemix, clonesTotal, clonesOriginal, clonesOnlyRemix, clonesBetween, "clonesRemix" + fileNumber + ".csv");
                	nameOriginal = new String[numberOfProjectsInOneFile];
                    nameRemix = new String[numberOfProjectsInOneFile];
                    clonesOriginal = new int[numberOfProjectsInOneFile];
                    clonesOnlyRemix = new int[numberOfProjectsInOneFile];
                    clonesTotal = new int[numberOfProjectsInOneFile];
                    clonesBetween = new int[numberOfProjectsInOneFile];
                }
            }
            int numberRestFiles = (numberFiles / 2) % numberOfProjectsInOneFile;
            if(numberRestFiles > 0) {
                fileNumber++;
                
                // Avoid null-values in the csv-file.
                String[] nameOriginalNew = new String[numberRestFiles];
                String[] nameRemixNew = new String[numberRestFiles];
                int[] clonesOriginalNew = new int[numberRestFiles];
                int[] clonesOnlyRemixNew = new int[numberRestFiles];
                int[] clonesTotalNew = new int[numberRestFiles];
                int[] clonesBetweenNew = new int[numberRestFiles];
                System.arraycopy(nameOriginal, 0, nameOriginalNew, 0, numberRestFiles);
                System.arraycopy(nameRemix, 0, nameRemixNew, 0, numberRestFiles);
                System.arraycopy(clonesOriginal, 0, clonesOriginalNew, 0, numberRestFiles);
                System.arraycopy(clonesOnlyRemix, 0, clonesOnlyRemixNew, 0, numberRestFiles);
                System.arraycopy(clonesTotal, 0, clonesTotalNew, 0, numberRestFiles);
                System.arraycopy(clonesBetween, 0, clonesBetweenNew, 0, numberRestFiles);
                CSVWriter.writeCSVRemix(nameOriginalNew, nameRemixNew, clonesTotalNew, clonesOriginalNew, clonesOnlyRemixNew, clonesBetweenNew, "clonesRemix" + fileNumber + ".csv");
            }
    	} catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
    /**
     * This main-method returns the total number of clones from json-files.
     */
    public static void mainTotal(String[] args) {
    	try {
    		int numberInOneFile = 100;
    		int numberFiles = folder.listFiles().length;
    		int[] numberClones = new int[numberInOneFile];
    		String[] projectName = new String[numberInOneFile];
    		int count = 0;
    		int countFile = 0;
    		for(int i = 0; i < numberFiles; i++) {
    			count++;
    		    Project project = null;
                final File fileEntry = Objects.requireNonNull(folder.listFiles())[i];
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println("------------");
                    System.out.println("Projekt: " + fileEntry.getName());
                    System.out.println("------------");
                    try {
                        project = JsonParser.parseRaw(fileEntry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(project != null) {
                    	String name = project.getName();
                        Preparation preparation = new Preparation();
                        List<String> preparatedProject = preparation.codePreparation(project);
                        Normalization norm = new Normalization();
                        List<String> normalizedProject = norm.codeNormalization(preparatedProject);
                        ComparisonAlgorithm comp = new ComparisonAlgorithm();
                        List<List<CloneBlock>> allClones = comp.findAllClones(normalizedProject);
                        Formatting form = new Formatting();
                        List<List<ClonePairCode>> formattedCode = form.formatting(allClones, preparatedProject);
                        int numberOfClones = 0;
                        for(List<ClonePairCode> pair : formattedCode) {
                    	    numberOfClones = numberOfClones + pair.size();
                        }
                        numberClones[i % numberInOneFile] = numberOfClones;
                        projectName[i % numberInOneFile] = name;
                    }
                }
                if(count == numberInOneFile) {
                	count = 0;
                	countFile++;
                	String fileName = "allClones" + countFile + ".csv";
            		CSVWriter.writeCSVOnlyTotal(numberClones, projectName, fileName);
            		numberClones = new int[numberInOneFile];
            		projectName = new String[numberInOneFile];
                }
    		}
    		countFile++;
    		int rest = numberFiles % numberInOneFile;
    		int[] numberClonesNew = new int[rest];
    		String[] projectNameNew = new String[rest];
    		String fileName = "allClones" + countFile + ".csv";
    		System.arraycopy(numberClones, 0, numberClonesNew, 0, rest);
    		System.arraycopy(projectName, 0, projectNameNew, 0, rest);
    		CSVWriter.writeCSVOnlyTotal(numberClonesNew, projectNameNew, fileName);
    	} catch (Exception e) {
            e.printStackTrace();
        }
    }
}

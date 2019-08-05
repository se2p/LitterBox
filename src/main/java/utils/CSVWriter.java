package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import clone.structure.ClonePairCode;

public class CSVWriter {

	private final static String folder = "...";
	private static Path path;
	
	/**
	 * Writes the CSV-file with the total number of clones, the clones in the stage,
	 * the clones in the sprites and the clones between sprites to the home directory.
	 * @param formattedCode The code of the clones
	 * @param nameOfFile the name of CSV-file
	 */
	public static void writeCSV(List<List<List<ClonePairCode>>> formattedCode, String nameOfFile, List<String> projectName) {
		path = Paths.get(folder, nameOfFile);
		try(BufferedWriter writeBuffer = Files.newBufferedWriter(path)) {
			String header = String.format("%s;%s;%s;%s", "Name", "Total", "Between_Sprites", "Sprites");
	        writeBuffer.write(header +"\n");
	    	for(int r = 0; r < formattedCode.size(); r++) {
	    		try {
		        int[] numberOfClones = new int[formattedCode.get(r).size()];
		        for(int i = 0; i < numberOfClones.length; i++) {
			        numberOfClones[i] = formattedCode.get(r).get(i).size();
		        }
		        int numberTotal = 0;
		        for(int j : numberOfClones) {
			        numberTotal += j;
		        }
		        List<String> sprites = new ArrayList<String>();
		        for(int i = 1; i < numberOfClones.length - 1; i++) {
			        sprites.add("Sprite" + i);
		        }
		        int[] numberSprites = new int[numberOfClones.length - 2];
		        for(int i = 1; i < numberSprites.length - 1; i++) {
			        numberSprites[i - 1] = numberOfClones[i];
		        }
		        int spriteClones = numberOfClones[0];
		        for(int i = 1; i < numberOfClones.length - 1; i++) {
	    		    spriteClones += numberOfClones[i];
			    }
			    String line = String.format("%s;%d;%d;%d", projectName.get(r), numberTotal, numberOfClones[numberOfClones.length - 1], spriteClones);
			    writeBuffer.write(line + "\n");
	    		} catch(Exception e) {
	    			continue;
	    		}
		    }
		} catch(IOException e) {
	    	e.printStackTrace();
    	}
	}
	
	/**
	 * Writes the CSV-file with the total number of clones, the clones in the original 
	 * project, the clones between the projects and the clones who are additionally 
	 * in the remix.
	 * @param total The total number of clones.
	 * @param original The number of clones in the original.
	 * @param remix The number of clones in the remix.
	 * @param between The number of clones between the projects.
	 * @param nameOfFile The name of the CSV-file.
	 */
	public static void writeCSVRemix(String[] nameOriginal, String[] nameRemix, int[] total, int[] original, int[] remix, int[] between, String nameOfFile) {
		path = Paths.get(folder, nameOfFile);
		try(BufferedWriter writeBuffer = Files.newBufferedWriter(path)) {
			String header = String.format("%s;%s;%s;%s;%s;%s", "Original", "Remix", "Total", "Clones_Original", "Clones_Only_Remix", "Clones_Between_Projects");
			writeBuffer.write(header + "\n");
			for(int i = 0; i < nameOriginal.length; i++) {
				String line = String.format("%s;%s;%d;%d;%d;%d", nameOriginal[i], nameRemix[i], total[i], original[i], remix[i], between[i]);
				writeBuffer.write(line + "\n");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeCSVOnlyTotal(int[] numberOfClones, String[] names, String nameOfFile) {
		path = Paths.get(folder, nameOfFile);
		try(BufferedWriter writeBuffer = Files.newBufferedWriter(path)) {
			String header = String.format("%s;%s", "Project_Name", "Number_Of_Clones");
			writeBuffer.write(header + "\n");
			for(int i = 0; i < numberOfClones.length; i++) {
			    String total = String.format("%s;%d", names[i], numberOfClones[i]);
			    writeBuffer.write(total + "\n");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
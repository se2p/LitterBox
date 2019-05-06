package clone.detection;

import java.util.ArrayList;
import java.util.List;

import clone.structure.CloneBlock;
import clone.structure.ClonePairCode;

/**
 * This class matches the code lines to the original code.
 */
public class Formatting {

	/**
	 * The code lines are matched to the original code.
	 * @param clones The list of the clones.
	 * @param original The original code.
	 * @return A list of the cloned code.
	 */
	public List<List<ClonePairCode>> formatting(List<List<CloneBlock>> clones, List<String> original) {
		List<List<ClonePairCode>> codePairs = new ArrayList<List<ClonePairCode>>();
		for(List<CloneBlock> list : clones) {
			List<ClonePairCode> spriteCode = new ArrayList<ClonePairCode>();
		    for(CloneBlock bl : list) {
			    List<String> firstBlock = new ArrayList<String>();
			    List<String> secondBlock = new ArrayList<String>();
			    for(int i = bl.getFirstLineBlockOne(); i <= bl.getLastLineBlockOne(); i++) {
			    	String firstLine = original.get(i);
			    	firstBlock.add(firstLine);
			    }
			    for(int i = bl.getFirstLineBlockTwo(); i <= bl.getLastLineBlockTwo(); i++) {
			    	String secondLine = original.get(i);
			    	secondBlock.add(secondLine);
			    }
			    spriteCode.add(new ClonePairCode(firstBlock, secondBlock));
		    }
		    codePairs.add(spriteCode);
		}
		return codePairs;
	}
	
	/**
	 * Matches the code lines of the original project and the of the remix to the code.
	 * @param clones The lines of the code.
	 * @param original The code of the original project.
	 * @param remix The code of the remix.
	 * @return The clones as code.
	 */
	public List<List<ClonePairCode>> formattingRemix(List<List<CloneBlock>> clones, List<String> original, List<String> remix) {
		List<List<ClonePairCode>> code = new ArrayList<List<ClonePairCode>>();
		List<ClonePairCode> clonesBetweenProjects = formatterBetweenProjects(clones, original, remix);
		List<ClonePairCode> clonesOriginal = formatterOriginal(clones, original);
		List<ClonePairCode> clonesRemix = formatterRemix(clones, remix);
		code.add(clonesBetweenProjects);
		code.add(clonesOriginal);
		code.add(clonesRemix);
		return code;
	}
	
	// Assign the clone pairs between the projects to the code. 
	private List<ClonePairCode> formatterBetweenProjects(List<List<CloneBlock>> clones, List<String> original, List<String> remix) {
		List<ClonePairCode> clonesBetweenProjects = new ArrayList<ClonePairCode>();
		for(CloneBlock clone : clones.get(0)) {
			List<String> blockRemix = new ArrayList<String>();
			List<String> blockOriginal = new ArrayList<String>();
			for(int i = clone.getFirstLineBlockOne(); i <= clone.getLastLineBlockOne(); i++) {
				String firstLine = remix.get(i);
				blockRemix.add(firstLine);
			}
			for(int i = clone.getFirstLineBlockTwo(); i <= clone.getLastLineBlockTwo(); i++) {
				String secondLine = original.get(i);
				blockOriginal.add(secondLine);
			}
			clonesBetweenProjects.add(new ClonePairCode(blockRemix, blockOriginal));
	    }
		return clonesBetweenProjects;
	}
	
	// Assign the clone pairs of the original Project to the code.
	private List<ClonePairCode> formatterOriginal(List<List<CloneBlock>> clones, List<String> original) {
		List<ClonePairCode> clonesOriginal = new ArrayList<ClonePairCode>();
		for(CloneBlock clone : clones.get(1)) {
			List<String> blockOne = new ArrayList<String>();
			List<String> blockTwo = new ArrayList<String>();
			for(int i = clone.getFirstLineBlockOne(); i <= clone.getLastLineBlockOne(); i++) {
				String firstLine = original.get(i);
				blockOne.add(firstLine);
			}
			for(int i = clone.getFirstLineBlockTwo(); i <= clone.getLastLineBlockTwo(); i++) {
				String secondLine = original.get(i);
				blockTwo.add(secondLine);
			}
			clonesOriginal.add(new ClonePairCode(blockOne, blockTwo));
		}
		return clonesOriginal;
	}
	
	// Assign the clone pairs of the original Project to the code.
	private List<ClonePairCode> formatterRemix(List<List<CloneBlock>> clones, List<String> remix) {
		List<ClonePairCode> clonesRemix = new ArrayList<ClonePairCode>();
		for(CloneBlock clone : clones.get(2)) {
			List<String> blockOne = new ArrayList<String>();
			List<String> blockTwo = new ArrayList<String>();
			for(int i = clone.getFirstLineBlockOne(); i <= clone.getLastLineBlockOne(); i++) {
				String firstLine = remix.get(i);
				blockOne.add(firstLine);
			}
			for(int i = clone.getFirstLineBlockTwo(); i <= clone.getLastLineBlockTwo(); i++) {
				String secondLine = remix.get(i);
				blockTwo.add(secondLine);
			}
			clonesRemix.add(new ClonePairCode(blockOne, blockTwo));
		}
		return clonesRemix;
	}
}

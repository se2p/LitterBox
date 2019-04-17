package clone.detection;

import java.util.ArrayList;
import java.util.List;

import clone.structure.CloneBlock;
import clone.structure.ClonePairCode;
import clone.structure.ClonePairLine;

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
			    for(ClonePairLine clonePair : bl.getBlock()) {
				    String firstLine = original.get(clonePair.getLineOne());
				    String secondLine = original.get(clonePair.getLineTwo());
				    firstBlock.add(firstLine);
				    secondBlock.add(secondLine);
			    }
			    spriteCode.add(new ClonePairCode(firstBlock, secondBlock));
		    }
		    codePairs.add(spriteCode);
		}
		return codePairs;
	}
}

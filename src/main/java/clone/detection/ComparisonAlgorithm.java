package clone.detection;

import java.util.ArrayList;
import java.util.List;

import clone.structure.CloneBlock;
import clone.structure.ClonePairLine;

/**
 * This class implements the comparison algorithm from Ducasse to detect clones 
 * in the normalized code. 
 */
public class ComparisonAlgorithm {

	private List<String> normalizedCode;
	private List<ClonePairLine> coordinateSystem;
	
	public ComparisonAlgorithm(List<String> normalizedCode) {
		this.normalizedCode = normalizedCode;
		coordinateSystem = new ArrayList<ClonePairLine>();
	}
	
	public List<CloneBlock> findClones() {
		fillCoordinateSystem();
		List<CloneBlock> cloneBlock = findCloneBlocks();
		return cloneBlock;
	}
	
	private void fillCoordinateSystem() {
		for(int i = 0; i < normalizedCode.size(); i++) {
			for(int j = 0; j < normalizedCode.size(); j++) {
				if(normalizedCode.get(i).equals(normalizedCode.get(j)) && i != j) {
					boolean cloneAlreadyInclude = false;
					for(int k = 0; k < coordinateSystem.size(); k++) {
						if(coordinateSystem.get(k).getLineOne() == j && 
							coordinateSystem.get(k).getLineTwo() == i) {
							cloneAlreadyInclude = true;
						}
					}
					if(!cloneAlreadyInclude) {
						coordinateSystem.add(new ClonePairLine(i, j));
					}
				}
			}
		}
	}
	
	private List<CloneBlock> findCloneBlocks() {
		List<CloneBlock> cloneBlock = new ArrayList<CloneBlock>();
		for(int i = 0; i < coordinateSystem.size(); i++) {
			CloneBlock block = new CloneBlock();
			int x = coordinateSystem.get(i).getLineOne();
			int y = coordinateSystem.get(i).getLineTwo();
			block.addPair(coordinateSystem.get(i));
			for(int j = 0; j < coordinateSystem.size(); j++) {
				while(x + 1 < normalizedCode.size() && 
					y + 1 < normalizedCode.size() && 
					coordinateSystem.get(j).getLineOne() == x + 1 &&
					coordinateSystem.get(j).getLineTwo() == y + 1) {
					block.addPair(coordinateSystem.get(j));
					x++;
					y++;
				}
			}
			
			// Only clones with more than one clone pairs will be listed.
			if(!isSubsetOfOtherBlock(block, cloneBlock) && block.getBlock().size() > 1) {
				cloneBlock.add(block);
			}
		}
		return cloneBlock;
	}
	
	private boolean isSubsetOfOtherBlock(CloneBlock block, List<CloneBlock> list) {
		for(CloneBlock bl : list) {
			boolean[] allIncluded = new boolean[block.getBlock().size()];
			for(int i = 0; i < block.getBlock().size(); i++) {
				allIncluded[i] = bl.contains(block.getBlock().get(i));
			}
			boolean allTrue = true;
			for(boolean b : allIncluded) {
				allTrue = allTrue && b;
			}
			if(allTrue) {
				return true;
			}
		}
		return false;
	}
}

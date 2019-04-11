package clone.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * This saves a block who's cloned.
 */
public class CloneBlock {

	private List<ClonePairLine> block;
	
	public CloneBlock() {
		block = new ArrayList<ClonePairLine>();
	}
	
	/**
	 * Adds a clone pair to the block
	 * @param pair the clone pair to add
	 */
	public void addPair(ClonePairLine pair) {
		block.add(pair);
	}
	
	/**
	 * This is the getter for the block.
	 * @return the block
	 */
	public List<ClonePairLine> getBlock(){
		return block;
	}
	
	public boolean contains(ClonePairLine pair) {
		for(ClonePairLine cp : block) {
			if(cp.equals(pair)) {
				return true;
			}
		}
		return false;
	}
}

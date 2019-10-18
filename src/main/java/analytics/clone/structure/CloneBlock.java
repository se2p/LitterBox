/**
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package analytics.clone.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * This saves a block who's cloned.
 */
public class CloneBlock {

	private List<ClonePairLine> block;
	
	/**
	 * Creates a new CloneBlock with an empty block.
	 */
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
	
	/**
	 * Checks whether the pair is in the block.
	 * @param pair the pair to check
	 * @return true, if the pair is in the block, else false
	 */
	public boolean contains(ClonePairLine pair) {
		for(ClonePairLine cp : block) {
			if(cp.equals(pair)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the the x-value of the first line in this block.
	 * @return The x-value of the first line.
 	 */
	public int getFirstLineBlockOne() {
		return block.get(0).getLineOne();
	}
	
	/**
	 * Returns the the y-value of the first line in this block.
	 * @return The y-value of the first line.
 	 */
	public int getFirstLineBlockTwo() {
		return block.get(0).getLineTwo();
	}
	
	/**
	 * Returns the the x-value of the last line in this block.
	 * @return The x-value of the last line.
 	 */
	public int getLastLineBlockOne() {
		int lastLine = block.size() - 1;
		return block.get(lastLine).getLineOne();
	}
	
	/**
	 * Returns the the y-value of the last line in this block.
	 * @return The y-value of the last line.
 	 */
	public int getLastLineBlockTwo() {
		int lastLine = block.size() - 1;
		return block.get(lastLine).getLineTwo();
	}
	
	/**
	 * Checks if the object {@link o} is equal to this.
	 * @param o The object to compare.
	 */
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof CloneBlock)) {
			return false;
		}
		CloneBlock toCompare = (CloneBlock) o;
		for(ClonePairLine pair: block) {
			if(!toCompare.contains(pair)) {
				return false;
			}
		}
		for(ClonePairLine pair : toCompare.getBlock()) {
			if(!this.contains(pair)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * The toString method of this object.
	 * @return The string who presents this object.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(ClonePairLine p : block) {
			sb.append("(");
			sb.append(p.getLineOne());
			sb.append(",");
			sb.append(p.getLineTwo());
			sb.append(")");
			sb.append(System.getProperty("line.separator"));
		}
		sb.append("----------");
		return sb.toString();
	}
}

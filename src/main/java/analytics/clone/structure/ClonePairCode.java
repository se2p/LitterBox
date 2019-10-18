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

import java.util.List;

/**
 * This class saves the code of two cloned blocks.
 */
public class ClonePairCode {

	private List<String> blockOne;
	private List<String> blockTwo;
	private int[] sprites; 
	
	/**
	 * Creates a new ClonePairCode with the given blocks.
	 * @param blockOne The first code block.
	 * @param blockTwo The clone of the code block.
	 * @param sprites The sprites where the clone is.
	 */
	public ClonePairCode(List<String> blockOne, List<String> blockTwo, int[] sprites) {
		this.blockOne = blockOne;
		this.blockTwo = blockTwo;
		this.sprites = sprites;
	}
	
	/**
	 * Creates a new ClonePairCode with the given blocks.
	 * @param blockOne The first code block.
	 * @param blockTwo The clone of the code block.
	 */
	public ClonePairCode(List<String> blockOne, List<String> blockTwo) {
		this.blockOne = blockOne;
		this.blockTwo = blockTwo;
	}
	
	/**
	 * The getter for the first block.
	 * @return blockOne
	 */
	public List<String> getBlockOne() {
		return blockOne;
	}
	
	/**
	 * The getter for the second block.
	 * @return blockTwo
	 */
	public List<String> getBlockTwo() {
		return blockTwo;
	}
	
	/**
	 * The getter for the sprites.
	 * @return The sprites of the clones.
	 */
	public int[] getSprites() {
		return sprites;
	}
}

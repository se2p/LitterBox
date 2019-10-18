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

/**
 * This class saves the lines of pairs of clones.
 */
public class ClonePairLine {

	private int lineOne;
	private int lineTwo;
	
	/**
	 * Creates a ClonePairLine with the two given numbers.
	 * @param lineOne The x-value of the pair.
	 * @param lineTwo The y-value of the pair.
	 */
	public ClonePairLine(int lineOne, int lineTwo) {
		this.lineOne = lineOne;
		this.lineTwo = lineTwo;
	}

	/**
	 * This is the getter for the first line.
	 * @return the lineOne
	 */
	public int getLineOne() {
		return lineOne;
	}


	/** 
	 * This is the getter for the second line.
	 * @return the lineTwo
	 */
	public int getLineTwo() {
		return lineTwo;
	}
	
	/**
	 * Checks if the given object is equal to this.
	 * @param object The object to compare.
	 * @return True if the object is equal, false if not.
	 */
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof ClonePairLine)) {
			return false;
		}
		ClonePairLine toCompare = (ClonePairLine) object;
		if(toCompare.getLineOne() == lineOne && toCompare.getLineTwo() == lineTwo) {
			return true;
		}
		return false;
	}
}

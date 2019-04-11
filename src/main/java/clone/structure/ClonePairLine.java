package clone.structure;

/**
 * This class saves the lines of pairs of clones.
 */
public class ClonePairLine {

	private int lineOne;
	private int lineTwo;
	
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

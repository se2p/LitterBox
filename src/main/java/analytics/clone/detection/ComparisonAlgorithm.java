package analytics.clone.detection;

import java.util.ArrayList;
import java.util.List;

import analytics.clone.structure.CloneBlock;
import analytics.clone.structure.ClonePairLine;

/**
 * This class implements the comparison algorithm from Ducasse to detect clones 
 * in the normalized code. 
 */
public class ComparisonAlgorithm {
	
	/**
	 * This method searchs for clones in sprites and between the sprites.
	 * @param normalizedCode The normalized code of the project.
	 * @return A list with lists with clones. In the first list there are the 
	 *         clones in the stage and than the clones in the sprites and in the
	 *         last there are the clones between sprites.
	 */
	public List<List<CloneBlock>> findAllClones(List<String> normalizedCode) {
		List<CloneBlock> allClones = findClones(normalizedCode); 
		List<List<CloneBlock>> clonesInSprite = findClonesInSprites(normalizedCode);
		List<CloneBlock> clonesBetweenSprites = getClonesBetweenSprites(allClones, clonesInSprite);
		clonesInSprite.add(clonesBetweenSprites);
		return clonesInSprite;
	}
	
	/**
	 * This method searchs for clones with and without gaps.
	 * 
	 * @param normalizedCode The normalized code of the project.
	 * @return A list with two lists. In the first list are the clones without gaps
	 *         and in the second the clones with gaps.
	 */
	public List<List<CloneBlock>> findAllClonesGap(List<String> normalizedCode) {
		List<CloneBlock> allClones = findClones(normalizedCode);
		List<CloneBlock> clonesWithGap = getClonesWithGap(allClones);
		List<CloneBlock> clonesWithoutGap = getClonesWithoutGap(allClones, clonesWithGap);
		List<List<CloneBlock>> clones = new ArrayList<List<CloneBlock>>();
		clones.add(clonesWithoutGap);
		clones.add(clonesWithGap);
		return clones;
	}
	
	private List<CloneBlock> getClonesWithGap(List<CloneBlock> clones) {
		List<CloneBlock> clonesWithGap = new ArrayList<CloneBlock>();
		for(CloneBlock block : clones) {
			for(int i = 0; i < block.getBlock().size() - 1; i++) {
				if(block.getBlock().get(i).getLineOne() + 1 != block.getBlock().get(i + 1).getLineOne()) {
					clonesWithGap.add(block);
					System.out.println(block.toString());
					break;
				}
			}
		}
		return clonesWithGap;
	}
	
	private List<CloneBlock> getClonesWithoutGap(List<CloneBlock> allClones, List<CloneBlock> withGap) {
		List<CloneBlock> clonesWithoutGap = new ArrayList<CloneBlock>();
		for(CloneBlock block : allClones) {
			boolean blockIsWithGap = false;
			for(CloneBlock blockGap : withGap) {
				if(block.equals(blockGap)) {
					blockIsWithGap = true;
				}
			}
			if(!blockIsWithGap) {
				clonesWithoutGap.add(block);
			}
		}
		System.out.println("withoutGap:" + clonesWithoutGap.size());
		return clonesWithoutGap;
	}
	
	/**
	 * This method search for clones between the original code and the remix, 
	 * in the original code and in the remix.
	 * @param normalizedProject The normalized code of the original project.
	 * @param normalizedRemix The normalized code of the remix.
	 * @return A list of a list with the clones. In the first list there are 
	 *         the clones between the projects. In the second there are the 
	 *         clones in the original project and in the third there are the
	 *         clones in the remix.
	 */
	public List<List<CloneBlock>> findClonesRemix(List<String> normalizedProject, List<String> normalizedRemix) {
		List<List<CloneBlock>> allClones = new ArrayList<List<CloneBlock>>();
		List<ClonePairLine> coordinateSystem = fillCoordinateSystemRemix(normalizedProject, normalizedRemix);
		List<CloneBlock> clones = findCloneBlocksRemix(coordinateSystem, normalizedProject, normalizedRemix);
	    List<CloneBlock> clonesWithoutOld = removeIfCloneIsInOriginal(clones);
		List<CloneBlock> clonesBetweenProjects = checkCloneExistsTwice(clonesWithoutOld);
		List<CloneBlock> clonesOriginal = findClones(normalizedProject);
		List<CloneBlock> clonesRemix = findClones(normalizedRemix);
		List<CloneBlock> newClonesRemix = removeIfCloneIsOld(clonesRemix, clones);
		allClones.add(clonesBetweenProjects);
		allClones.add(clonesOriginal);
		allClones.add(newClonesRemix);
		return allClones;
	}

	/*
	 * All clones who are in the original project or between the project
	 * will be removed. 
	 */
	private List<CloneBlock> removeIfCloneIsOld(List<CloneBlock> clonesRemix, List<CloneBlock> clonesBetween) {
		List<CloneBlock> newClones = new ArrayList<CloneBlock>();
		List<CloneBlock> clonesToDelete = findClonesInOriginalAndRemix(clonesBetween);
		for(CloneBlock b : clonesRemix) {
			boolean toDelete = false;
			for(CloneBlock bl : clonesToDelete) {
				if(xIsEqual(b, bl)) {
					toDelete = true;
				}
			}
			if(!toDelete) {
				newClones.add(b);
			}
		}
		return newClones;
	}
	
	/*
	 * A coordinate system will be build and there is a point if the line 
	 * in the normalized remix code is equal to the line in the normalized
	 * original project code.
	 */
	private List<ClonePairLine> fillCoordinateSystemRemix(List<String> normalizedProject, List<String> normalizedRemix) {
		List<ClonePairLine> coordinateSystem = new ArrayList<ClonePairLine>();
		for(int i = 0; i < normalizedRemix.size(); i++) {
			for(int j = 0; j < normalizedProject.size(); j++) {
				if(normalizedRemix.get(i).equals(normalizedProject.get(j)) && !normalizedRemix.get(i).contains("~")) {
					coordinateSystem.add(new ClonePairLine(i, j));
				}
			}
		}
		return coordinateSystem;
	}
	
	/*
	 * Search for clone blocks by doing search for diagonals in the coordinate 
	 * system. And in the diagonal can be a hole of the length one.
	 */
	private List<CloneBlock> findCloneBlocksRemix(List<ClonePairLine> coordinateSystem, List<String> normalizedProject, List<String> normalizedRemix) {
		List<CloneBlock> cloneBlock = new ArrayList<CloneBlock>();
		for(int i = 0; i < coordinateSystem.size(); i++) {
			CloneBlock block = new CloneBlock();
			int x = coordinateSystem.get(i).getLineOne();
			int y = coordinateSystem.get(i).getLineTwo();
			block.addPair(coordinateSystem.get(i));
			for(int j = 0; j < coordinateSystem.size(); j++) {
				while(x + 1 < normalizedRemix.size() && 
					y + 1 < normalizedProject.size() && 
					(coordinateSystem.get(j).getLineOne() == x + 1 || coordinateSystem.get(j).getLineOne() == x + 2) &&
					(coordinateSystem.get(j).getLineTwo() == y + 1 || coordinateSystem.get(j).getLineTwo() == y + 2)) {
					block.addPair(coordinateSystem.get(j));
					x++;
					y++;
				}
			}
			
			// Only clones with more than four clone pairs will be listed.
			if(!isSubsetOfOtherBlock(block, cloneBlock) && block.getBlock().size() > 4) {
				cloneBlock.add(block);
			}
		}
		return cloneBlock;
	}
	
	/*
	 * Checks whether the founded clone is from the original code transfered or 
	 * if the clone exists twice then it is cloned.
	 */
	private List<CloneBlock> checkCloneExistsTwice(List<CloneBlock> clones) {
		List<CloneBlock> clonedCode = new ArrayList<CloneBlock>();
		
		// Counts how often this clone exists. Two clones a equal, if they have the same y-Values.
		for(CloneBlock bl : clones) {
			int numberClones = 0;
			for(CloneBlock block : clones) {
				if(yIsEqual(bl, block)) {
					numberClones++;
				}
			}
			
			// If numberClones is 1 then there is no clone.
			if(numberClones > 1) {
				int numberIncluded = 0;
				for(CloneBlock b : clonedCode) {
					if(yIsEqual(bl, b)) {
						numberIncluded++;
					}
				}
				
				// In the result must always be one clone fewer than numberClones.
				if(numberIncluded < numberClones - 1) {
					clonedCode.add(bl);
				}
			}
		}
		return clonedCode;
	}

	// Checks if the two blocks have the same y-Values.
	private boolean yIsEqual(CloneBlock block1, CloneBlock block2) {
		int min = getMinLength(block1, block2);
		boolean[] yEqual = new boolean[min];
		for(int i = 0; i < yEqual.length; i++) {
			yEqual[i] = block1.getBlock().get(i).getLineTwo() == block2.getBlock().get(i).getLineTwo();
		}
		boolean allTrue = true;
		for(boolean all : yEqual) {
			allTrue = allTrue && all;
		}
		return allTrue;
	}
	
	// Check if the two blocks have the same x-Values.
	private boolean xIsEqual(CloneBlock block1, CloneBlock block2) {
		int min = getMinLength(block1, block2);
		boolean[] xEqual = new boolean[min];
		for(int i = 0; i < xEqual.length; i++) {
			xEqual[i] = block1.getBlock().get(i).getLineOne() == block2.getBlock().get(i).getLineOne();
		}
		boolean allTrue = true;
		for(boolean all : xEqual) {
			allTrue = allTrue && all;
		}
		return allTrue;
	}
	
	/*
	 * Deletes blocks if there is minimum one clone in the original project.
	 * Otherwise there will be to much clones in the result.
	 */
	private List<CloneBlock> removeIfCloneIsInOriginal(List<CloneBlock> clonesBetween) {
		
		/* 
		 * Search for clones who are in the original and in the remix and then
		 * divide this clones in unities. In one unity are equal clones.
		 */
		List<CloneBlock> found = findClonesInOriginalAndRemix(clonesBetween);
		List<List<CloneBlock>> dividedUnities = divideInUnities(found);
		for(List<CloneBlock> list : dividedUnities) {
			int numberOfClonesRemix = numberSameY(list);
			int numberOfClonesOriginal = list.size() / numberOfClonesRemix;
			
			/* 
			 * If number of clones in the remix is lower than the number of clones
			 * in the original then there is no clone and every block must be removed.
			 */
			if(numberOfClonesRemix <= numberOfClonesOriginal) {
				for(CloneBlock b : list) {
					for(CloneBlock bl : clonesBetween) {
						if(b.equals(bl)) {
							clonesBetween.remove(bl);
							break;
						}
					}
				}
			} else {
				
				/* 
				 * If number of clones in the remix is higher than the number of clones
				 * in the original then there are number of clones remix minus number
				 * of clones original clones. And other blocks will be deleted.
				 */
				int firstY = list.get(0).getFirstLineBlockTwo();
				List<CloneBlock> clonesToStay = new ArrayList<CloneBlock>();
				for(CloneBlock b : list) {
					if(b.getFirstLineBlockTwo() == firstY) {
						clonesToStay.add(b);
					}
				}
				for(int i = 0; i < numberOfClonesOriginal - 1; i++) {
					clonesToStay.remove(0);
				}
				clonesBetween = deleteOther(clonesBetween, clonesToStay, list);
			}
		}
		return clonesBetween;
	}
	
	// If there is a clone and in the remix is the same then this will be added.
	private List<CloneBlock> findClonesInOriginalAndRemix(List<CloneBlock> all) {
		List<CloneBlock> found = new ArrayList<CloneBlock>();
		for(int i = 0; i < all.size(); i++) {
			for(int j = 0; j < all.size(); j++) {
				
				// If the x-values are equal then this clone is in the original.
				boolean xEqual = xIsEqual(all.get(i), all.get(j));
				if(xEqual && i != j) {
					CloneBlock[] cloneInRemix = cloneInRemix(all, all.get(i), all.get(j));
					if(cloneInRemix != null && !found.contains(all.get(i))) {
						found.add(all.get(i));
					}
				}
			}
		}
		return found;
	}
	
	// Deletes the clone blocks who are not in toStay but in other from all.
	private List<CloneBlock> deleteOther(List<CloneBlock> all, List<CloneBlock> toStay, List<CloneBlock> other) {
		List<CloneBlock> rest = new ArrayList<CloneBlock>();
		List<CloneBlock> toDelete = new ArrayList<CloneBlock>();
		
		// Search for blocks who are in other but not in toStay.
		for(int i = 0; i < other.size(); i++) {
			boolean[] allTrue = new boolean[toStay.size()];
			for(int x = 0; x < allTrue.length; x++) {
				allTrue[x] = true;
			}
			for(int j = 0; j < toStay.size(); j++) {
				if(other.get(i).equals(toStay.get(j))) {
					allTrue[j] = false;
				}
			}
			boolean allT = true;
			for(boolean a : allTrue) {
				allT = allT && a;
			}
			if(allT) {
				toDelete.add(other.get(i));
			}
		}
		
		// Add block to rest if it is in all but not in toDelete.
		for(int i = 0; i < all.size(); i++) {
			boolean[] allTrue = new boolean[toDelete.size()];
			for(int z = 0; z < allTrue.length; z++) {
				allTrue[z] = true;
			}
			for(int j = 0; j < toDelete.size(); j++) {
				if(all.get(i).equals(toDelete.get(j))) {
					allTrue[j] = false;
				}
			}
			boolean allT = true;
			for(boolean b : allTrue) {
				allT = allT && b;
			}
			if(allT) {
				rest.add(all.get(i));
			}
		}
		return rest;
	}
	
	// Counts how often a block with the same y-value as the first block in the list exists.
	private int numberSameY(List<CloneBlock> list) {
		int number = 0;
		int y = list.get(0).getBlock().get(0).getLineTwo();
		for(CloneBlock b : list) {
			if(b.getBlock().get(0).getLineTwo() == y) {
				number++;
			}
		}
		return number;
	}
	
	// Divide the founded list in unities and in one unity are equal clones.
	private List<List<CloneBlock>> divideInUnities(List<CloneBlock> found){
		List<List<CloneBlock>> divided = new ArrayList<List<CloneBlock>>();
		List<Integer> alreadySeenX = new ArrayList<Integer>();
		List<Integer> alreadySeenY = new ArrayList<Integer>();
		
		// Count how many unities exists.
		int numberOfUnities = 0;
		for(int i = 0; i < found.size(); i++) {
			if(!(alreadySeenX.contains(found.get(i).getBlock().get(0).getLineOne()) 
				|| alreadySeenY.contains(found.get(i).getBlock().get(0).getLineTwo()))) {
				numberOfUnities++;
			}
			alreadySeenX.add(found.get(i).getBlock().get(0).getLineOne());
			alreadySeenY.add(found.get(i).getBlock().get(0).getLineTwo());
		}
		
		// Divide the list in the unities. A clone is equal either if x-value or y-value is the same.
		for(int i = 0; i < numberOfUnities; i++) {
			List<CloneBlock> block = new ArrayList<CloneBlock>();
			alreadySeenX = new ArrayList<Integer>();
			alreadySeenY = new ArrayList<Integer>();
			int firstNotNull = 0;
			while(found.get(firstNotNull) == null) {
				firstNotNull++;
			}
			block.add(found.get(firstNotNull));
			alreadySeenX.add(found.get(firstNotNull).getBlock().get(0).getLineOne());
			alreadySeenY.add(found.get(firstNotNull).getBlock().get(0).getLineTwo());
			found.set(firstNotNull, null);
			for(int j = 0; j < found.size(); j++) {
				if(found.get(j) != null && (alreadySeenX.contains(found.get(j).getBlock().get(0).getLineOne()) 
						|| alreadySeenY.contains(found.get(j).getBlock().get(0).getLineTwo()))) {
					block.add(found.get(j));
					alreadySeenX.add(found.get(j).getBlock().get(0).getLineOne());
					alreadySeenY.add(found.get(j).getBlock().get(0).getLineTwo());
					found.set(j, null);
				}
			}
			divided.add(block);
		}
		return divided;
	}
	
	// Search for two clone blocks with the same x-values two clone blocks with the same y-value.
 	private CloneBlock[] cloneInRemix(List<CloneBlock> clonesBetween, CloneBlock bl, CloneBlock block) {
		for(CloneBlock block1 : clonesBetween) {
			for(CloneBlock block2 : clonesBetween) {
				boolean sameX = xIsEqual(block1, block2);
				if(sameX) {
					if(!(block1.equals(bl) || block2.equals(block) || block1.equals(block) || block2.equals(bl))) {
						boolean yEqual = checkYEqual(bl, block, block1, block2);
						if(yEqual) {
							CloneBlock[] blocks = {block1, block2};
							return blocks;
						}
					}
				}
			}
		}
		return null;
	}
	
 	/*
 	 * Checks whether y-values of bl is equal to the y-value of block1 or block2 and 
 	 * y-value is of block is equal to block1 or block2.
 	 */
	private boolean checkYEqual(CloneBlock bl, CloneBlock block, CloneBlock block1, CloneBlock block2) {
		boolean allTrueBlB1 = yIsEqual(bl, block1);
		boolean allTrueBlB2 = yIsEqual(bl, block2);
		boolean allTrueBlockB1 = yIsEqual(block, block1);
		boolean allTrueBlockB2 = yIsEqual(block, block2);
		return (allTrueBlB1 && allTrueBlockB2) || (allTrueBlB2 && allTrueBlockB1);
	}
	
	// Search for clones between sprites.
	private List<CloneBlock> getClonesBetweenSprites(List<CloneBlock> allClones, List<List<CloneBlock>> clonesInSprite) {
		List<CloneBlock> clonesBetweenSprites = new ArrayList<CloneBlock>();
		for(CloneBlock bl : allClones) {
			boolean blIsInSprite = false;
			for(List<CloneBlock> list : clonesInSprite) {
				for(CloneBlock block : list) {
					if(bl.equals(block)) {
						blIsInSprite = true;
					}
				}
			}
			if(!blIsInSprite) {
				clonesBetweenSprites.add(bl);
			}
		}
		return clonesBetweenSprites;
	}

	// Search for clones in the normalized code.
    private List<CloneBlock> findClones(List<String> normalizedCode) {
		List<ClonePairLine> coordinateSystem = fillCoordinateSystem(normalizedCode);
		List<CloneBlock> cloneBlock = findCloneBlocks(coordinateSystem, normalizedCode);
		return cloneBlock;
	}
	
    /* 
     * Creates a coordinate system and there is a point at (x,y) if the line x 
     * in the normalized code is equal to the line y. When there is a point at 
     * (x,y) there will be no point at (y,x).
     */
	private List<ClonePairLine> fillCoordinateSystem(List<String> normalizedCode) {
		List<ClonePairLine> coordinateSystem = new ArrayList<ClonePairLine>();
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
		return coordinateSystem;
	}
	
	/*
	 * Search for clone blocks by doing search for diagonals in the coordinate 
	 * system. And in the diagonal can be a gap of the length one.
	 */
	private List<CloneBlock> findCloneBlocks(List<ClonePairLine> coordinateSystem, List<String> normalizedCode) {
		List<CloneBlock> cloneBlock = new ArrayList<CloneBlock>();
		for(int i = 0; i < coordinateSystem.size(); i++) {
			CloneBlock block = new CloneBlock();
			int x = coordinateSystem.get(i).getLineOne();
			int y = coordinateSystem.get(i).getLineTwo();
			block.addPair(coordinateSystem.get(i));
			for(int j = 0; j < coordinateSystem.size(); j++) {
				while(x + 1 < normalizedCode.size() && 
					y + 1 < normalizedCode.size() && 
					((coordinateSystem.get(j).getLineOne() == x + 1 && coordinateSystem.get(j).getLineTwo() == y + 1) ||
					(coordinateSystem.get(j).getLineOne() == x + 2 && coordinateSystem.get(j).getLineTwo() == y + 2))) {
					if(!block.contains(coordinateSystem.get(j))) {
					    block.addPair(coordinateSystem.get(j));
					}
					if(coordinateSystem.get(j).getLineOne() == x + 1 && coordinateSystem.get(j).getLineTwo() == y + 1) {
						x++;
						y++;
					}
					if(coordinateSystem.get(j).getLineOne() == x + 2 && coordinateSystem.get(j).getLineTwo() == y + 2) {
						x += 2;
						y += 2;
					}
					
				}
			}
			
			// Only clones with more than four clone pairs will be listed.
			if(!isSubsetOfOtherBlock(block, cloneBlock) && block.getBlock().size() > 4 && !checkClonesIsInHimSelf(block)) {
				cloneBlock.add(block);
			}
		}
		return cloneBlock;
	}
	
	private boolean checkClonesIsInHimSelf(CloneBlock toCheck) {
		int[] x = new int[toCheck.getBlock().size()];
		int[] y = new int[toCheck.getBlock().size()];
		for(int i = 0; i < toCheck.getBlock().size(); i++) {
			x[i] = toCheck.getBlock().get(i).getLineOne();
			y[i] = toCheck.getBlock().get(i).getLineTwo();
		}
		for(int i = 0; i < x.length; i++) {
			for(int j = 0; j < y.length; j++) {
				if(x[i] == y[j]) {
					return true;
				}
			}
		}
		return false;
	}
	
	// Checks if a block in list complete contains block.
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
	
	// Searchs for clones in stage and sprites.
	private List<List<CloneBlock>> findClonesInSprites(List<String> normalizedCode){
		List<List<String>> spriteCode = divideInSprites(normalizedCode);
		List<List<CloneBlock>> clonesInSprite = new ArrayList<List<CloneBlock>>();
		for(List<String> code : spriteCode) {
			clonesInSprite.add(findClones(code));
		}
		return getOriginalNumber(clonesInSprite, normalizedCode);
	}
	
	// Divides the normalized code in sprites.
	private List<List<String>> divideInSprites(List<String> normalizedCode){
		List<List<String>> spriteCode =  new ArrayList<List<String>>();
		int numberOfSprites = numberOfSprites(normalizedCode);
		int j = 0;
		for(int i = 1; i <= numberOfSprites; i++) {
		    List<String> sprite = new ArrayList<String>();
		    while(j < normalizedCode.size() && !normalizedCode.get(j).contains("~endSprite~")) {
	            sprite.add(normalizedCode.get(j));
	            j++;
		    }
		    j = j + 2;
		    spriteCode.add(sprite);
		}
		return spriteCode;
	}
	
	// Returns the number of sprites.
	private int numberOfSprites(List<String> normalizedCode) {
		String last = normalizedCode.get(normalizedCode.size() - 2);
		char[] lastBlock = last.toCharArray();
		int firstNumber = 0;
		while(!Character.isDigit(lastBlock[firstNumber])) {
			firstNumber++;
		}
		String spriteNumber = "";
		for(int i = firstNumber; i < lastBlock.length; i++) {
			spriteNumber += lastBlock[i];
		}
		int number = Integer.parseInt(spriteNumber);
		return number;
	}
	
	/* 
	 * Because of the division in sprites the clones in the sprites have false 
	 * line numbers. This method give the correct numbers.
	 */
	private List<List<CloneBlock>> getOriginalNumber(List<List<CloneBlock>> spriteLines, List<String> allCode) {
		List<List<CloneBlock>> originalNumber = new ArrayList<List<CloneBlock>>();
		int shift = 0;
		for(List<CloneBlock> sprite : spriteLines) {
			List<CloneBlock> spriteNumber = new ArrayList<CloneBlock>();
			for(CloneBlock bl : sprite) {
				CloneBlock block = new CloneBlock();
				for(int i = 0; i < bl.getBlock().size(); i++) {
				    block.addPair(new ClonePairLine(bl.getBlock().get(i).getLineOne() + shift,
				    		bl.getBlock().get(i).getLineTwo() + shift));
				}
				spriteNumber.add(block);
			}
			originalNumber.add(spriteNumber);
			
			// Remove the added sprite from allCode and increase shift as often as lines in this sprite.
			while(!allCode.isEmpty() && !allCode.get(0).contains("~endSprite~")) {
				allCode.remove(0);
				shift++;
			}
			
			// Remove the two lines who contain endSprite and increase shift by 2.
			if(!allCode.isEmpty()) {
		        allCode.remove(0);
		        allCode.remove(0);
		        shift = shift + 2;
			}
		}
		return originalNumber;
	}
	
	// Returns the minimum length of the two blocks.
	private int getMinLength(CloneBlock one, CloneBlock two) {
		int min;
		if(one.getBlock().size() > two.getBlock().size()) {
			min = two.getBlock().size();
		} else {
			min = one.getBlock().size();
		}
		return min;
	}
}

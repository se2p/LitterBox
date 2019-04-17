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
	
	public List<List<CloneBlock>> findAllClones(List<String> normalizedCode) {
		List<CloneBlock> allClones = findClones(normalizedCode);
		List<List<CloneBlock>> clonesInSprite = findClonesInSprites(normalizedCode);
		List<CloneBlock> clonesBetweenSprites = getClonesBetweenSprites(allClones, clonesInSprite);
		clonesInSprite.add(clonesBetweenSprites);
		return clonesInSprite;
	}
	
	private List<CloneBlock> getClonesBetweenSprites(List<CloneBlock> allClones, 
			List<List<CloneBlock>> clonesInSprite) {
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
	private List<CloneBlock> findClones(List<String> normalizedCode) {
		List<ClonePairLine> coordinateSystem = fillCoordinateSystem(normalizedCode);
		List<CloneBlock> cloneBlock = findCloneBlocks(coordinateSystem, normalizedCode);
		return cloneBlock;
	}
	
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
					coordinateSystem.get(j).getLineOne() == x + 1 &&
					coordinateSystem.get(j).getLineTwo() == y + 1) {
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
	
	private List<List<CloneBlock>> findClonesInSprites(List<String> normalizedCode){
		List<List<String>> spriteCode = devideInSprites(normalizedCode);
		List<List<CloneBlock>> clonesInSprite = new ArrayList<List<CloneBlock>>();
		for(List<String> code : spriteCode) {
			clonesInSprite.add(findClones(code));
		}
		return getOriginalNumber(clonesInSprite, normalizedCode);
	}
	
	private List<List<String>> devideInSprites(List<String> normalizedCode){
		List<List<String>> spriteCode =  new ArrayList<List<String>>();
		int numberOfSprites = numberOfSprites(normalizedCode);
		int j = 0;
		for(int i = 1; i <= numberOfSprites; i++) {
		    List<String> sprite = new ArrayList<String>();
		    while(j < normalizedCode.size() && !normalizedCode.get(j).contains("~endSprite~")) {
	            sprite.add(normalizedCode.get(j));
	            j++;
		    }
		    j++;
		    spriteCode.add(sprite);
		}
		return spriteCode;
	}
	
	private int numberOfSprites(List<String> normalizedCode) {
		String last = normalizedCode.get(normalizedCode.size() - 1);
		char[] lastBlock = last.toCharArray();
		char numberSprites = lastBlock[lastBlock.length - 1];
		int number = -1;
		if(Character.isDigit(numberSprites)) {
			number = Character.getNumericValue(numberSprites);
		}
		return number;
	}
	
	private List<List<CloneBlock>> getOriginalNumber(List<List<CloneBlock>> spriteLines, 
		    List<String> allCode) {
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
			while(!allCode.isEmpty() && !allCode.get(0).contains("~endSprite~")) {
				allCode.remove(0);
				shift++;
			}
			if(!allCode.isEmpty()) {
		        allCode.remove(0);
		        shift++;
			}
		}
		return originalNumber;
	}
}

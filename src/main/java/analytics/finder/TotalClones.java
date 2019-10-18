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
package analytics.finder;

import java.util.ArrayList;
import java.util.List;

import analytics.IssueFinder;
import analytics.IssueReport;
import analytics.clone.detection.ComparisonAlgorithm;
import analytics.clone.detection.Formatting;
import analytics.clone.detection.Normalization;
import analytics.clone.detection.Preparation;
import analytics.clone.structure.CloneBlock;
import analytics.clone.structure.ClonePairCode;
import scratch.structure.Project;
import scratch.structure.Sprite;

/**
 * Checks for clones in the project with more than 4 blocks. Between these 
 * blocks can be a gap of the size 1.
 */
public class TotalClones implements IssueFinder {
	
	private String name = "total_clones";

	@Override
	public IssueReport check(Project project) {
		List<List<ClonePairCode>> formattedCode = getFormattedCode(project);
        int numberOfClones = 0;
        for(List<ClonePairCode> pair : formattedCode) {
        	numberOfClones = numberOfClones + pair.size();
        }
        String notes = "There are no clones.";
        if (numberOfClones > 0) {
            notes = "There are clones in the project.";
        }
        List<String> spriteNames = new ArrayList<String>();
        for(Sprite sprite : project.getSprites()) {
        	spriteNames.add(sprite.getName());
        }
        List<String> pos = createPosition(formattedCode, spriteNames);
        return new IssueReport(name, numberOfClones, pos, project.getPath(), notes);
	}

	@Override
	public String getName() {
		return name;
	}
	
	private List<List<ClonePairCode>> getFormattedCode(Project project) {
        Preparation preparation = new Preparation();
        
        // This is the original code separated in scripts.
        List<String> preparatedCode = preparation.codePreparation(project);
        Normalization norm = new Normalization();
        
        // This is the code who is normalized.
        List<String> normalizedCode = norm.codeNormalization(preparatedCode);
        ComparisonAlgorithm compare = new ComparisonAlgorithm();
        
        // These are the blocks who are cloned saved as int tuples.
        List<List<CloneBlock>> blocks = compare.findAllClones(normalizedCode);
        Formatting formatting = new Formatting();
        
        // These are the blocks who are cloned saved as String tuples.
        return formatting.formatting(blocks, preparatedCode);
	}
	
	private List<String> createPosition(List<List<ClonePairCode>> formattedCode, List<String> spriteNames) {
		List<String> pos = new ArrayList<String>();
        List<ClonePairCode> stageClone = formattedCode.get(0);
        int clonesInStage = stageClone.size();
        if(clonesInStage > 0) {
        	pos.add(clonesInStage +" time(s) in the Stage");
        }
        for(int i = 1; i < formattedCode.size() - 1; i++) {
        	int clonesInSprite = formattedCode.get(i).size();
        	if(clonesInSprite > 0) {
        	    pos.add(clonesInSprite + " time(s) in " + spriteNames.get(i - 1));
        	}
        }
        List<ClonePairCode> betweenSprites = formattedCode.get(formattedCode.size() - 1);
        List<int[]> writed = new ArrayList<int[]>();
        if(betweenSprites.size() > 0) {
        	for(ClonePairCode clone : betweenSprites) {
        		int numberClone = 1;
        		for(ClonePairCode clone1 : betweenSprites) {
        			if(clone.getSprites()[0] == clone1.getSprites()[0] && clone.getSprites()[1] == clone1.getSprites()[1] && clone != clone1) {
        				numberClone++;
        			}
        		}
        		int[] toAdd = {clone.getSprites()[0], clone.getSprites()[1]};
        		boolean toAddAdded = false;
        		for(int[] toCheck : writed) {
        			if(toCheck[0] == toAdd[0] && toCheck[1] == toAdd[1]) {
        				toAddAdded = true;
        				break;
        			}
        		}
        		if(!toAddAdded) {
        		    if(clone.getSprites()[0] == 0) {
        			    pos.add(numberClone + " time(s) between the Stage and " + spriteNames.get(clone.getSprites()[1] - 1));
        		    } else if(clone.getSprites()[1] == 0) {
        			    pos.add(numberClone + " time(s) between " + spriteNames.get(clone.getSprites()[0] - 1) + "and the Stage");
        		    } else {
        			    pos.add(numberClone + " time(s) between " + spriteNames.get(clone.getSprites()[0] - 1) + " and " + spriteNames.get(clone.getSprites()[1] - 1));
        		    }
        		    writed.add(toAdd);
        		}
        	}
    	}
        return pos;
	}
}

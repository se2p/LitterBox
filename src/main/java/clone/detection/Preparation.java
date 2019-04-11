package clone.detection;

import java.util.ArrayList;
import java.util.List;

import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Sprite;

/**
 * This class prepares the code for the normalization and reads the scripts.
 */
public class Preparation {
	
	/**
	 * Joins the scripts and make a string list.
	 * @param project The project who saves the script.
	 * @return The script as a string list.
	 */
	public List<String> codePreparation(Project project){
		List<Script> script = joinScriptLists(project);
		List<String> blockScript = getContentFromScript(script);
		return blockScript;
	}
	
	private List<Script> joinScriptLists(Project project) {
		List<Script> script = project.getStage().getScripts();
		List<Sprite> sprites = project.getSprites();
		for(Sprite sp : sprites) {
        	List<Script> spriteScript = sp.getScripts();
            script.addAll(spriteScript);
        }
		return script;
	}
	
	private List<String> getContentFromScript(List<Script> script) {
		List<String> scripts = new ArrayList<String>();
		int scriptNr = 0;
		for(Script sc : script) {
			scriptNr++;
			for(ScBlock bl : sc.getBlocks()) {
				getContentFromBlock(bl, scripts);
			}
			scripts.add("~endBlock~" + scriptNr);
		}
		return scripts;
	}
	
	private List<String> getContentFromBlock(ScBlock block, List<String> scripts){
	    scripts.add(block.getContent());
		if (block.getNestedBlocks() != null) {
            for (ScBlock nested : block.getNestedBlocks()) {
            	getContentFromBlock(nested, scripts);
            }
        }
		if (block.getElseBlocks() != null) {
            for (ScBlock elseBl : block.getElseBlocks()) {
            	getContentFromBlock(elseBl, scripts);
            }
        }
		return scripts;
	}
}

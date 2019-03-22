package scratch2.structure;

import scratch2.data.*;
import utils.JsonParser;

import java.util.List;

/**
 * Represents the background of the project. The stage is similar to a
 * Sprite.
 * The stage does not require a costume.
 */
public class Stage extends Scriptable {

    /**
     * @param name        Name of the project
     * @param scripts     List containing Scripts
     * @param comments    List containing Comments
     * @param variables   List of ScVariables
     * @param lists       List of ScLists
     * @param costumes    List of Costumes
     * @param sounds      List of Sounds
     * @param initCostume The current selected Costume
     */
    public Stage(String name, List<Script> scripts, List<Comment> comments, List<ScVariable> variables,
                 List<ScList> lists, List<Costume> costumes, List<Sound> sounds, int initCostume) {
        super(name, scripts, comments, variables, lists, costumes, sounds, initCostume);
    }

    @Override
    public String toString() {
        return ("---------------------" + "\n") +
                "Name: " + this.getName() + "\n" +
                JsonParser.prettyPrintScript(this.getScripts()) + "\n" +
                "Comments: " + this.getComments() + "\n" +
                "Variables: " + this.getVariables() + "\n" +
                "Lists: " + this.getLists() + "\n" +
                "Costumes: " + this.getCostumes() + "\n" +
                "Sounds: " + this.getSounds() + "\n" +
                "initCostume: " + this.getInitCostume() + "\n" +
                "---------------------";
    }

}

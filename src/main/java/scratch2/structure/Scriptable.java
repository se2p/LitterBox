package scratch2.structure;

import scratch2.data.*;

import java.util.List;
import java.util.Map;

/**
 * Superclass for all scriptable objects (Stage and Sprite)
 */
public class Scriptable {

    private String name;
    private List<Script> scripts;
    private List<Comment> comments;
    private List<ScVariable> variables;
    private List<ScList> lists;
    private List<Costume> costumes;
    private List<Sound> sounds;
    private int initCostume;

    /**
     * @param scripts      List containing Scripts
     * @param comments     List containing Comments
     * @param variables    List of Variables
     * @param lists        List of ScList
     * @param costumes     List of Costumes
     * @param sounds       List of Sounds
     * @param initCostume  The current selected Costume
     */
    public Scriptable(String name, List<Script> scripts, List<Comment> comments, List<ScVariable> variables,
                      List<ScList> lists, List<Costume> costumes, List<Sound> sounds, int initCostume) {
        this.name = name;
        this.scripts = scripts;
        this.comments = comments;
        this.variables = variables;
        this.lists = lists;
        this.costumes = costumes;
        this.sounds = sounds;
        this.initCostume = initCostume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setVariables(List<ScVariable> variables) {
        this.variables = variables;
    }

    public void setLists(List<ScList> lists) {
        this.lists = lists;
    }

    public void setCostumes(List<Costume> costumes) {
        this.costumes = costumes;
    }

    public void setSounds(List<Sound> sounds) {
        this.sounds = sounds;
    }

    public void setInitCostume(int initCostume) {
        this.initCostume = initCostume;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<ScVariable> getVariables() {
        return variables;
    }

    public List<ScList> getLists() {
        return lists;
    }

    public List<Costume> getCostumes() {
        return costumes;
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public int getInitCostume() {
        return initCostume;
    }
}

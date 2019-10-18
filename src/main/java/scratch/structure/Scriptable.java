/*
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
package scratch.structure;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.*;

import java.util.List;

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
    private JsonNode blockStack;

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
                      List<ScList> lists, List<Costume> costumes, List<Sound> sounds, int initCostume, JsonNode blockStack) {
        this.name = name;
        this.scripts = scripts;
        this.comments = comments;
        this.variables = variables;
        this.lists = lists;
        this.costumes = costumes;
        this.sounds = sounds;
        this.initCostume = initCostume;
        this.blockStack = blockStack;
    }

    public JsonNode getBlockStack() {
        return blockStack;
    }

    public void setBlockStack(JsonNode blockStack) {
        this.blockStack = blockStack;
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

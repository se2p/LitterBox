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
package scratch.structure;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.*;
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
                 List<ScList> lists, List<Costume> costumes, List<Sound> sounds, int initCostume, JsonNode blockStack) {
        super(name, scripts, comments, variables, lists, costumes, sounds, initCostume, blockStack);
    }

    @Override
    public String toString() {
        return ("---------------------" + "\n") +
                "Name: " + this.getName() + "\n" +
                JsonParser.prettyPrintScript3(this.getScripts()) + "\n" +
                "Comments: " + this.getComments() + "\n" +
                "Variables: " + this.getVariables() + "\n" +
                "Lists: " + this.getLists() + "\n" +
                "Costumes: " + this.getCostumes() + "\n" +
                "Sounds: " + this.getSounds() + "\n" +
                "initCostume: " + this.getInitCostume() + "\n" +
                "---------------------";
    }

}

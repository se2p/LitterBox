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
import java.util.Arrays;
import java.util.List;
import scratch.data.Comment;
import scratch.data.Costume;
import scratch.data.ScList;
import scratch.data.ScVariable;
import scratch.data.Script;
import scratch.data.Sound;
import utils.JsonParser;

/**
 * A scriptable object displayed on the project stage.
 * Sprites require a Costume.
 */
public class Sprite extends Scriptable {

    private double[] position;
    private double rotation;
    private String rotationStyle;
    private int size;

    /**
     * @param name          Name of Sprite
     * @param scripts       List containing Scripts
     * @param comments      List containing Comments
     * @param variables     List of ScVariables
     * @param lists         List of ScList
     * @param costumes      List of Costumes
     * @param sounds        List of Sounds
     * @param initCostume   The current selected Costume
     * @param position      Position of Sprite on Stage
     * @param rotation      Rotation of Sprite
     * @param rotationStyle RotationStyle
     * @param size          Scale of Sprite
     */
    public Sprite(String name, List<Script> scripts, List<Comment> comments, List<ScVariable> variables,
                  List<ScList> lists, List<Costume> costumes, List<Sound> sounds, int initCostume, JsonNode blockStack,
                  double[] position, double rotation, String rotationStyle, int size) {
        super(name, scripts, comments, variables, lists, costumes, sounds, initCostume, blockStack);
        this.position = position;
        this.rotation = rotation;
        this.rotationStyle = rotationStyle;
        this.size = size;
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
                "Position: " + Arrays.toString(this.getPosition()) + "\n" +
                "Rotation: " + this.getRotation() + "\n" +
                "RotationStyle: " + this.getRotationStyle() + "\n" +
                "Scale: " + this.getSize() + "\n" +
                "---------------------";
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public String getRotationStyle() {
        return rotationStyle;
    }

    public void setRotationStyle(String rotationStyle) {
        this.rotationStyle = rotationStyle;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}

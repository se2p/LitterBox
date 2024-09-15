/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
 * implementation note: class instead of record, since @JsonUnwrapped does not work (yet) for record attributes
 */
public final class RawTarget {
    private boolean isStage;
    private String name;
    private Map<RawBlockId, RawVariable<?>> variables;
    private Map<RawBlockId, RawList> lists;
    private Map<RawBlockId, String> broadcasts;
    private Map<RawBlockId, RawBlock> blocks;
    private Map<RawBlockId, RawComment> comments;
    private int currentCostume;
    private List<RawCostume> costumes;
    private List<RawSound> sounds;
    private Integer layerOrder;
    private Double volume;
    @JsonUnwrapped
    private StageAttributes stageAttributes;
    @JsonUnwrapped
    private SpriteAttributes spriteAttributes;

    public boolean isStage() {
        return isStage;
    }

    public void setIsStage(boolean stage) {
        isStage = stage;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<RawBlockId, RawVariable<?>> variables() {
        return variables;
    }

    public void setVariables(
            Map<RawBlockId, RawVariable<?>> variables) {
        this.variables = variables;
    }

    public Map<RawBlockId, RawList> lists() {
        return lists;
    }

    public void setLists(
            Map<RawBlockId, RawList> lists) {
        this.lists = lists;
    }

    public Map<RawBlockId, String> broadcasts() {
        return broadcasts;
    }

    public void setBroadcasts(
            Map<RawBlockId, String> broadcasts) {
        this.broadcasts = broadcasts;
    }

    public Map<RawBlockId, RawBlock> blocks() {
        return blocks;
    }

    public void setBlocks(
            Map<RawBlockId, RawBlock> blocks) {
        this.blocks = blocks;
    }

    public Map<RawBlockId, RawComment> comments() {
        return comments;
    }

    public void setComments(
            Map<RawBlockId, RawComment> comments) {
        this.comments = comments;
    }

    public int currentCostume() {
        return currentCostume;
    }

    public void setCurrentCostume(int currentCostume) {
        this.currentCostume = currentCostume;
    }

    public List<RawCostume> costumes() {
        return costumes;
    }

    public void setCostumes(List<RawCostume> costumes) {
        this.costumes = costumes;
    }

    public List<RawSound> sounds() {
        return sounds;
    }

    public void setSounds(List<RawSound> sounds) {
        this.sounds = sounds;
    }

    public Integer layerOrder() {
        return layerOrder;
    }

    public void setLayerOrder(Integer layerOrder) {
        this.layerOrder = layerOrder;
    }

    public Double volume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public StageAttributes stageAttributes() {
        return stageAttributes;
    }

    public void setStageAttributes(
            StageAttributes stageAttributes) {
        this.stageAttributes = stageAttributes;
    }

    public SpriteAttributes spriteAttributes() {
        return spriteAttributes;
    }

    public void setSpriteAttributes(
            SpriteAttributes spriteAttributes) {
        this.spriteAttributes = spriteAttributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof RawTarget that) {
            return this.isStage == that.isStage
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.variables, that.variables)
                    && Objects.equals(this.lists, that.lists)
                    && Objects.equals(this.broadcasts, that.broadcasts)
                    && Objects.equals(this.blocks, that.blocks)
                    && Objects.equals(this.comments, that.comments)
                    && this.currentCostume == that.currentCostume
                    && Objects.equals(this.costumes, that.costumes)
                    && Objects.equals(this.sounds, that.sounds)
                    && Objects.equals(this.layerOrder, that.layerOrder)
                    && Double.doubleToLongBits(this.volume) == Double.doubleToLongBits(that.volume)
                    && Objects.equals(this.stageAttributes, that.stageAttributes)
                    && Objects.equals(this.spriteAttributes, that.spriteAttributes);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                isStage, name, variables, lists, broadcasts, blocks, comments, currentCostume, costumes, sounds,
                layerOrder, volume, stageAttributes, spriteAttributes
        );
    }

    @Override
    public String toString() {
        return "RawTarget["
                + "isStage=" + isStage + ", "
                + "name=" + name + ", "
                + "variables=" + variables + ", "
                + "lists=" + lists + ", "
                + "broadcasts=" + broadcasts + ", "
                + "blocks=" + blocks + ", "
                + "comments=" + comments + ", "
                + "currentCostume=" + currentCostume + ", "
                + "costumes=" + costumes + ", "
                + "sounds=" + sounds + ", "
                + "layerOrder=" + layerOrder + ", "
                + "volume=" + volume + ", "
                + "stageAttributes=" + stageAttributes + ", "
                + "spriteAttributes=" + spriteAttributes
                + ']';
    }

    public record StageAttributes(
            Integer tempo, String videoState, Double videoTransparency, String textToSpeechLanguage
    ) {
    }

    public record SpriteAttributes(
            boolean visible, double x, double y, double size, double direction, boolean draggable, String rotationStyle
    ) {
    }
}

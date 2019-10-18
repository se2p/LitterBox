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
import utils.Version;

import java.util.List;

/**
 * The Project class holds all content of a Scratch project file
 */
public class Project {

    private String name;
    private String path;
    private String filenameExtension;
    private Stage stage;
    private List<Sprite> sprites;
    private Version version;

    /**
     * The default Constructor, which creates the whole Project object
     *
     * @param name              The name of the project
     * @param path              The path to the project file
     * @param filenameExtension The file format used to load this project
     * @param stage             The Stage class
     * @param sprites           List of all Sprites
     */
    public Project(String name, String path, String filenameExtension, Stage stage, List<Sprite> sprites) {
        this.name = name;
        this.path = path;
        this.filenameExtension = filenameExtension;
        this.stage = stage;
        this.sprites = sprites;
    }

    public Project() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("~~~~~~~~~~~~~~~~~~~~~" + "\n");
        sb.append("Name: ").append(this.getName()).append("\n");
        sb.append("Path: ").append(this.getPath()).append("\n");
        sb.append("Extension: ").append(this.getFilenameExtension()).append("\n");
        sb.append("Version: ").append(this.getVersion().toString()).append("\n");
        sb.append(stage.toString()).append("\n");
        if (sprites != null) {
            for (Sprite s : sprites) {
                sb.append(s.toString()).append("\n");
            }
        }
        sb.append("~~~~~~~~~~~~~~~~~~~~~");
        return sb.toString();
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getFilenameExtension() {
        return filenameExtension;
    }

    public Stage getStage() {
        return stage;
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFilenameExtension(String filenameExtension) {
        this.filenameExtension = filenameExtension;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setSprites(List<Sprite> sprites) {
        this.sprites = sprites;
    }

}

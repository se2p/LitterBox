package scratch2.structure;

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
        sb.append(stage.toString()).append("\n");
        if (sprites != null) {
            for (Sprite s : sprites) {
                sb.append(s.toString()).append("\n");
            }
        }
        sb.append("~~~~~~~~~~~~~~~~~~~~~");
        return sb.toString();
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

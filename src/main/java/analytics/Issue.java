package analytics;

import java.util.List;

/**
 * Wrapper class that simulates a issue and holds different information
 */
public class Issue {

    private String name;
    private int count;
    private List<String> position;
    private String projectPath;
    private String notes;

    /**
     *
     * @param count How often the Issue appears
     * @param position [0] = stage, [1],[2],... = sprites
     * @param projectPath The projects path
     * @param notes Notes defined by each IssueFinder
     */
    public Issue(String name, int count, List<String> position, String projectPath, String notes) {
        this.name = name;
        this.count = count;
        this.position = position;
        this.projectPath = projectPath;
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Issue ").append(name).append(" was found ").append(count).append(" time(s).");
        if(position != null && position.size() > 0) {
            sb.append("\nPosition: ").append(position);
        }
        sb.append("\nProject: ").append(projectPath);
        sb.append("\nNotes: ").append(notes);
        sb.append("\n--------------------------------------------");
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getPosition() {
        return position;
    }

    public void setPosition(List<String> position) {
        this.position = position;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

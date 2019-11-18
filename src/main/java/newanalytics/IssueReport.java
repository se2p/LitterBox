package newanalytics;

import java.util.List;

/**
 * Wrapper class that simulates a issue and holds different information
 */
public class IssueReport {

    private String name;
    private int count;
    private List<String> position;
    private String notes;

    /**
     *
     * @param count How often the IssueReport appears
     * @param position [0] = stage, [1],[2],... = sprites
     * @param notes Notes defined by each IssueFinder
     */
    public IssueReport(String name, int count, List<String> position, String notes) {
        this.name = name;
        this.count = count;
        this.position = position;

        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Issue ").append(name).append(" was found ").append(count).append(" time(s).");
        if(position != null && position.size() > 0) {
            sb.append("\nPosition: ").append(position);
        }
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

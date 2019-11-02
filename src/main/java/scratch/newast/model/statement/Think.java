package scratch.newast.model.statement;

public class Think implements SpriteLookStmt {
    private String thought;

    public Think(String thought) {
        this.thought = thought;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }
}
package scratch.newast.model.statement;

public class Say implements SpriteLookStmt {
    private String string;

    public Say(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
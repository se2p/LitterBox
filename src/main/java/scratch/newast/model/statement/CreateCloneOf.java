package scratch.newast.model.statement;

import scratch.newast.model.Sprite;

public class CreateCloneOf implements CommonStmt {
    private Sprite sprite;

    public CreateCloneOf(Sprite sprite) {
        this.sprite = sprite;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
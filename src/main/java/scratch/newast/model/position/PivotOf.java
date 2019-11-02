package scratch.newast.model.position;

import scratch.newast.model.Sprite;

public class PivotOf implements Position {
    private Sprite sprite;

    public PivotOf(Sprite sprite) {
        this.sprite = sprite;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
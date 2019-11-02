package scratch.newast.model.sound;

public class SoundWithNum implements Sound {
    private java.lang.Number num;

    public SoundWithNum(java.lang.Number num) {
        this.num = num;
    }

    public java.lang.Number getNum() {
        return num;
    }

    public void setNum(java.lang.Number num) {
        this.num = num;
    }
}
package scratch.data;

/**
 * Wrapper for Scratch Sounds
 */
public class Sound {

    private String name;
    private String assetId;
    private String dataFormat;

    public Sound() {

    }

    @Override
    public String toString() {
        return "Sound{ " + assetId +
                " name='" + name + '\'' +
                ", dataFormat=" + dataFormat +
                '}';
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

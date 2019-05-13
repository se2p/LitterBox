package scratch.data;

/**
 * Wrapper for Scratch Costumes
 * Default background: White
 */
public class Costume {

    private String assetId;
    private String name = "#FFF";
    private String dataFormat;

    public Costume() {

    }

    @Override
    public String toString() {
        return "Costume{ " + assetId +
                " name='" + name + '\'' +
                ", dataFormat=" + dataFormat +
                '}';
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

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

}

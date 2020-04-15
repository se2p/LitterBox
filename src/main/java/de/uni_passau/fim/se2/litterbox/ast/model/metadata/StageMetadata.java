package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class StageMetadata extends ActorMetadata {
    private int tempo;
    private int videoTransparency;
    private String videoState;
    private String textToSpeechLanguage;

    public StageMetadata(CommentMetadataList commentsMetadata, VariableMetadataList variables, ListMetadataList lists
            , BroadcastMetadataList broadcasts, int currentCostume, ImageMetadataList costumes,
                         SoundMetadataList sounds, int volume, int layerOrder, int tempo, int videoTransparency,
                         String videoState, String textToSpeechLanguage) {
        super(commentsMetadata, variables, lists, broadcasts, currentCostume, costumes, sounds, volume, layerOrder);
        this.tempo = tempo;
        this.videoTransparency = videoTransparency;
        this.videoState = videoState;
        this.textToSpeechLanguage = textToSpeechLanguage;
    }

    public int getTempo() {
        return tempo;
    }

    public int getVideoTransparency() {
        return videoTransparency;
    }

    public String getVideoState() {
        return videoState;
    }

    public String getTextToSpeechLanguage() {
        return textToSpeechLanguage;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}

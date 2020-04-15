package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class StageMetadata extends ActorMetadata {
    private int tempo;
    private int videoTransparency;
    private String videoState;
    private String textToSpeechLanguage;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}

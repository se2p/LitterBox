package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TextToSpeechBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor, TextToSpeechExtensionVisitor {

    public static final String NAME = "text_to_speech_block_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visitParentVisitor(TextToSpeechBlock node){
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(TextToSpeechBlock node) {
        node.accept((TextToSpeechExtensionVisitor) this);
    }

    @Override
    public void visit(SetLanguage node) {
        count++;
    }

    @Override
    public void visit(SetVoice node) {
        count++;
    }

    @Override
    public void visit(Speak node) {
        count++;
    }

}

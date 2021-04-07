package de.uni_passau.fim.se2.litterbox.ast.model.statement.texttospeech.language;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedLanguageBlock extends AbstractNode implements Language {
    private final BlockMetadata metadata;
    private FixedLanguageType type;

    public FixedLanguageBlock(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedLanguageType.fromString(typeName);
        this.metadata =metadata;
    }

    public FixedLanguageType getType() {
        return type;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }


    public enum FixedLanguageType {

        //TODO
        GERMAN("de");

        private final String type;

        FixedLanguageType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedLanguageType fromString(String type) {
            for (FixedLanguageType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedLanguage: " + type);
        }

        public String getType() {
            return type;
        }
    }

}

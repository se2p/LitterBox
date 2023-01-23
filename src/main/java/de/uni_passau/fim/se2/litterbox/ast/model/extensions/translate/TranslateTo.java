package de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;

public class TranslateTo extends AbstractNode implements StringExpr, TranslateExpression {
    private final StringExpr text;
    private final TLanguage language;
    private final BlockMetadata metadata;

    public TranslateTo(StringExpr text, TLanguage language, BlockMetadata metadata) {
        super(text, language, metadata);
        this.language = language;
        this.text = text;
        this.metadata = metadata;
    }

    public StringExpr getText() {
        return text;
    }

    public TLanguage getLanguage() {
        return language;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit((TranslateBlock) this);
    }

    @Override
    public void accept(TranslateExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public Opcode getOpcode() {
        return StringExprOpcode.translate_getTranslate;
    }

    public Opcode getMenuLanguageOpcode() {
        return DependentBlockOpcode.translate_menu_languages;
    }
}

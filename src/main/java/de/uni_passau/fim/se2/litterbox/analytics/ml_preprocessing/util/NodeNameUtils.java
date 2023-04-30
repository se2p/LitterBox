package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScriptNameVisitor;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Node name utils.
 */
public class NodeNameUtils {

    /**
     * The constant DEFAULT_SPRITE_NAMES.
     */
    public static final List<String> DEFAULT_SPRITE_NAMES = Stream.of(
            "Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš", "Hahmo", "Kihusika", "Kukla", "Lik",
            "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia", "Postava", "Pêlîstik", "Sprait",
            "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning", "Umlingisi", "Veikėjas",
            "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن", "تەن", "شکلک", "สไปรต์",
            "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"
    ).map(String::toLowerCase).collect(Collectors.toUnmodifiableList());

    /**
     * Gets script entity name.
     * in case of @param node is Type Script, generate a name.
     * in case of @param node is Type ProcedureDefinition, return the original name
     *
     * @param node the node
     * @return the script entity name
     */
    public static Optional<String> getScriptEntityName(ScriptEntity node) {
        if (node instanceof Script)
            return Optional.of("ScriptId_" +  node.getScratchBlocks().hashCode());
        else if (node instanceof ProcedureDefinition)
            return Optional.of("Sprite_id_" + getParentSpriteName(node) +
                    "_ProcedureId_" + StringUtil.replaceSpecialCharacters(((ProcedureDefinition) node).getIdent().getName()));
        else return Optional.empty();
    }

    /**
     * Normalize sprite name string.
     *
     * @param spriteName the sprite name
     * @return the string
     */
    public static String normalizeSpriteName(String spriteName) {
        String normalizedSpriteLabel = StringUtil.normalizeName(spriteName);
        if (normalizedSpriteLabel.isEmpty() || isDefaultName(normalizedSpriteLabel)) {
            return null;
        }
        List<String> splitNameParts = StringUtil.splitToSubtokens(spriteName);
        String splitName = normalizedSpriteLabel;
        if (!splitNameParts.isEmpty()) {
            splitName = String.join("|", splitNameParts);
        }
        if(splitName.length()> 100)
            return splitName.substring(0, Math.min(splitName.length(), 100));
        return splitName;
    }

    private static String getParentSpriteName(ASTNode node) {
        return normalizeSpriteName(AstNodeUtil.findActor(node).get().getIdent().getName());
    }

    private static boolean isDefaultName(String normalizedSpriteLabel) {
        return DEFAULT_SPRITE_NAMES.contains(normalizedSpriteLabel);
    }
}

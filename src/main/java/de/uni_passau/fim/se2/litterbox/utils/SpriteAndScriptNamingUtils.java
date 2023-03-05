package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO rename
public class SpriteAndScriptNamingUtils {

    public static String generateScriptName(Script script) {
        ActorDefinition parentSprite = AstNodeUtil.findActor(script).get();
        return "spriteName_" + normalizeSpriteName(parentSprite.getIdent().getName()) + "_scriptId_" + parentSprite.getScripts().getScriptList().indexOf(script);
    }
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
        return splitName;
    }
    public static boolean isDefaultName(String normalizedSpriteLabel) {
        return DEFAULT_SPRITE_NAMES.contains(normalizedSpriteLabel);
    }
    public static final List<String> DEFAULT_SPRITE_NAMES = Stream.of(
            "Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš", "Hahmo", "Kihusika", "Kukla", "Lik",
            "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia", "Postava", "Pêlîstik", "Sprait",
            "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning", "Umlingisi", "Veikėjas",
            "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن", "تەن", "شکلک", "สไปรต์",
            "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"
    ).map(String::toLowerCase).collect(Collectors.toUnmodifiableList());
}

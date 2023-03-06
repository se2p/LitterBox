package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO rename
public class SpriteAndScriptNamingUtils {

    public static String getSpriteOrProcedureDefinitionName(ScriptEntity scriptEntity) {
        if (scriptEntity instanceof Script)
            return generateScriptName(scriptEntity);
        else if (scriptEntity instanceof ProcedureDefinition)
            return ((ProcedureDefinition) scriptEntity).getIdent().getName();
        else return null;
    }

    public static String generateScriptName(ScriptEntity node) {
        ActorDefinition parentSprite = AstNodeUtil.findActor(node).get();
        return "spriteName_" + normalizeSpriteName(parentSprite.getIdent().getName()) + getSpriteOrProcedureDefinitionIndex(node, parentSprite);
    }

    private static String getSpriteOrProcedureDefinitionIndex(ASTNode node, ActorDefinition parentSprite) {
        if (node instanceof Script)
            return "ScriptId_" + parentSprite.getScripts().getScriptList().indexOf(node);
        else if (node instanceof ProcedureDefinition)
            return "ProcedureId_" + parentSprite.getProcedureDefinitionList().getList().indexOf(node);
        else return null;
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

package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO rename?
public class NodeNameUtils {

    public static final List<String> DEFAULT_SPRITE_NAMES = Stream.of(
            "Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš", "Hahmo", "Kihusika", "Kukla", "Lik",
            "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia", "Postava", "Pêlîstik", "Sprait",
            "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning", "Umlingisi", "Veikėjas",
            "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن", "تەن", "شکلک", "สไปรต์",
            "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"
    ).map(String::toLowerCase).collect(Collectors.toUnmodifiableList());


    // TODO generate unique names
    public static String getSpriteOrProcedureDefinitionFullName(ScriptEntity scriptEntity) {
        ActorDefinition parentSprite = AstNodeUtil.findActor(scriptEntity).get();
        return generateParentSpriteName(parentSprite) + "_" + getSpriteOrProcedureDefinitionName(scriptEntity, parentSprite);
    }

    public static String generateParentSpriteName(ActorDefinition parentSprite) {
        return "_spriteName_" + normalizeSpriteName(parentSprite.getIdent().getName());
    }

    private static String getSpriteOrProcedureDefinitionName(ASTNode node, ActorDefinition parentSprite) {
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
}

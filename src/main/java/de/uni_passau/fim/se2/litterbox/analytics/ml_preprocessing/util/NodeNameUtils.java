package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodeNameUtils {

    private static final Logger log = Logger.getLogger(NodeNameUtils.class.getName());

    public static final List<String> DEFAULT_SPRITE_NAMES = Stream.of(
            "Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš", "Hahmo", "Kihusika", "Kukla", "Lik",
            "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia", "Postava", "Pêlîstik", "Sprait",
            "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning", "Umlingisi", "Veikėjas",
            "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن", "تەن", "شکلک", "สไปรต์",
            "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"
    ).map(String::toLowerCase).collect(Collectors.toUnmodifiableList());

    /**
     * a valid script is a script that contains at least more than 1 statement
     */
    public static boolean isValidScript(ScriptEntity scriptEntity) throws IllegalArgumentException {
        var stmts = getStatements(scriptEntity);
        return (stmts != null) && stmts.size() > 1;
    }

    private static List<Stmt> getStatements(ScriptEntity scriptEntity) {
        List<Stmt> stmts = null;
        if (scriptEntity instanceof Script) {
            stmts = ((Script) scriptEntity).getStmtList().getStmts();
        } else if (scriptEntity instanceof ProcedureDefinition) {
            stmts = ((ProcedureDefinition) scriptEntity).getStmtList().getStmts();
        }
        return stmts;
    }

    public static Optional<String> getScriptEntityName(ASTNode node) {
        if (node instanceof Script)
            return Optional.of("ScriptId_" + AstNodeUtil.findActor(node).get() + "_" + getBlockId(((Script) node).getStmtList()));
        else if (node instanceof ProcedureDefinition)
            return Optional.of("ProcedureId_" + AstNodeUtil.findActor(node).get() + "_" + getBlockId(((ProcedureDefinition) node).getStmtList()));
        else return Optional.empty();
    }

    private static String getBlockId(StmtList stmtList) {
        var firstStmt = getFirstNonDataBlockStmt(stmtList.getStmts());
        if (firstStmt != null)
            return String.valueOf(((NonDataBlockMetadata) firstStmt.getMetadata()).getBlockId().hashCode());
        else {
            log.severe("can't generate script id for " + stmtList.getStmts().get(0).getScratchBlocks());
            return null;
        }
    }

    private static Stmt getFirstNonDataBlockStmt(List<Stmt> stmts) {
        for (Stmt stmt : stmts) {
            if (stmt.getMetadata() instanceof NonDataBlockMetadata)
                return stmt;
        }
        return null;
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

    private static boolean isDefaultName(String normalizedSpriteLabel) {
        return DEFAULT_SPRITE_NAMES.contains(normalizedSpriteLabel);
    }
}

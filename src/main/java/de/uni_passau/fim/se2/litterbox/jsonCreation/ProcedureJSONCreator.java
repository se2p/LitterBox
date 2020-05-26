package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

public class ProcedureJSONCreator {
    public static String createProcedureJSONString(ProcedureDefinition definition, SymbolTable symbol) {
        StringBuilder jsonString = new StringBuilder();
        TopNonDataBlockMetadata defMetadata = (TopNonDataBlockMetadata) definition.getMetadata().getDefinition();
        NonDataBlockMetadata protoMetadata = (NonDataBlockMetadata) definition.getMetadata().getPrototype();
        //TODO do procedure definition and prototype here

        if (definition.getStmtList().getStmts().size()>0) {
            StmtListJSONCreator stmtListJSONCreator =
                    new StmtListJSONCreator(defMetadata.getBlockId(), definition.getStmtList(), symbol);
            jsonString.append(stmtListJSONCreator.createStmtListJSONString());
        }
        return jsonString.toString();
    }
}

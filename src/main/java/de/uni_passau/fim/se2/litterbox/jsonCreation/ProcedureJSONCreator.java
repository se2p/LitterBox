package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.*;

public class ProcedureJSONCreator {
    public static String createProcedureJSONString(ProcedureDefinition definition,
                                                   String actorName, SymbolTable symbol,
                                                   ProcedureDefinitionNameMapping procDefNameMapping) {
        StringBuilder jsonString = new StringBuilder();
        TopNonDataBlockMetadata defMetadata = (TopNonDataBlockMetadata) definition.getMetadata().getDefinition();
        NonDataBlockMetadata protoMetadata = (NonDataBlockMetadata) definition.getMetadata().getPrototype();
        String protoId = protoMetadata.getBlockId();
        StmtList stmtList = definition.getStmtList();
        String nextId = null;

        //TODO do procedure definition and prototype here
        if (stmtList.getStmts().size() > 0) {
            IdVisitor vis = new IdVisitor();
            nextId = vis.getBlockId(stmtList.getStmts().get(0));
        }

        StrId id = (StrId) definition.getIdent();
        ProcedureInfo procInfo = procDefNameMapping.getProcedures().get(actorName).get(id);

        //create definition block
        List<String> inputs = new ArrayList<>();
        inputs.add(createReferenceInput(CUSTOM_BLOCK_KEY, INPUT_SAME_BLOCK_SHADOW, protoMetadata.getBlockId(), false));
        jsonString.append(createBlockWithoutMutationString(defMetadata, nextId,
                null, createInputs(inputs), EMPTY_VALUE)).append(",");

        //create parameters
        List<ParameterDefinition> parameterDefinitions =
                definition.getParameterDefinitionList().getParameterDefinitions();
        List<ParameterInfo> parameterInfos = new ArrayList<>();
        for (ParameterDefinition parameterDefinition : parameterDefinitions) {
            parameterInfos.add(createParameters(jsonString, protoId, parameterDefinition));
            jsonString.append(",");
        }

        //todo prototype

        if (stmtList.getStmts().size() > 0) {
            StmtListJSONCreator stmtListJSONCreator =
                    new StmtListJSONCreator(defMetadata.getBlockId(), definition.getStmtList(), symbol);
            jsonString.append(",");
            jsonString.append(stmtListJSONCreator.createStmtListJSONString());
        }
        return jsonString.toString();
    }

    private static ParameterInfo createParameters(StringBuilder jsonString, String prototypeID,
                                                  ParameterDefinition parameterDefinition) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) parameterDefinition.getMetadata();
        jsonString.append(createBlockWithoutMutationString(metadata,
                null,
                prototypeID, EMPTY_VALUE, createFields(metadata.getFields().getList().get(0).getFieldsName(),
                        parameterDefinition.getIdent().getName(), null)));
        return new ParameterInfo(parameterDefinition.getIdent().getName(), metadata.getBlockId(),
                parameterDefinition.getType());

    }

    private static class ParameterInfo {
        String name;
        String defaultValue;
        String id;

        public ParameterInfo(String name, String id, Type type) {
            this.name = name;
            this.id = id;
            if (type instanceof BooleanType) {
                defaultValue = "\\\"false\\\"";
            } else {
                defaultValue = "\\\"\\\"";
            }
        }

        public String getName() {
            return name;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getId() {
            return id;
        }
    }
}



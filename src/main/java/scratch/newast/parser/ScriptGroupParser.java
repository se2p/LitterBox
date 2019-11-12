package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import scratch.newast.ParsingException;
import scratch.newast.model.DeclarationList;
import scratch.newast.model.DeclarationStmt;
import scratch.newast.model.EntityType;
import scratch.newast.model.Script;
import scratch.newast.model.ScriptGroup;
import scratch.newast.model.ScriptList;
import scratch.newast.model.SetStmtList;
import scratch.newast.model.expression.bool.Bool;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.procedure.ProcedureDeclarationList;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.resource.ResourceList;
import scratch.newast.model.statement.common.SetAttributeTo;
import scratch.newast.model.statement.common.SetStmt;
import scratch.newast.model.variable.Identifier;

public class ScriptGroupParser {

    public static ScriptGroup parse(JsonNode scriptGroupNode) throws ParsingException {
        Preconditions.checkNotNull(scriptGroupNode);
        Preconditions.checkArgument(scriptGroupNode.has("isStage"), "Missing field isStage in ScriptGroup");
        Preconditions.checkArgument(scriptGroupNode.has("name"), "Missing field name in ScriptGroup");

        EntityType entityType;
        if (scriptGroupNode.get("isStage").asBoolean()) {
            entityType = EntityType.stage;
        } else {
            entityType = EntityType.sprite;
        }

        Identifier identifier = new Identifier(scriptGroupNode.get("name").asText());

        List<Resource> res = ResourceParser.parseSound(scriptGroupNode.get("sounds"));
        res.addAll(ResourceParser.parseCostume(scriptGroupNode.get("costumes")));
        ResourceList resources = new ResourceList(res);

        List<DeclarationStmt> decls = DeclarationParser.parseLists(scriptGroupNode.get("lists"), identifier.getValue(),
                scriptGroupNode.get("isStage").asBoolean());
        decls.addAll(DeclarationParser.parseBroadcasts(scriptGroupNode.get("broadcasts"), identifier.getValue(),
                scriptGroupNode.get("isStage").asBoolean()));
        decls.addAll(DeclarationParser.parseVariables(scriptGroupNode.get("variables"), identifier.getValue(),
                scriptGroupNode.get("isStage").asBoolean()));
        DeclarationList declarations = new DeclarationList(decls);

        JsonNode allBlocks = scriptGroupNode.get("blocks");
        Iterator<String> fieldIterator = allBlocks.fieldNames();
        Iterable<String> iterable = () -> fieldIterator;
        Stream<String> stream = StreamSupport.stream(iterable.spliterator(), false);
        List<String> topLevelNodes = stream.filter(fieldName -> allBlocks.get(fieldName).get("topLevel").asBoolean())
                .collect(Collectors.toList());
        List<Script> scripts = new LinkedList<>();
        for (String topLevelID : topLevelNodes) {
            Script script = ScriptParser.parse(topLevelID, allBlocks);
            scripts.add(script);
        }
        ScriptList scriptList = new ScriptList(scripts);

        ProcedureDeclarationList procDeclList = ProcDeclParser.parse(allBlocks);

        SetStmtList setStmtList = parseSetStmts(scriptGroupNode);

        return new ScriptGroup(entityType, identifier, resources, declarations, setStmtList, procDeclList, scriptList);
    }

    private static SetStmtList parseSetStmts(JsonNode scriptGroupNode) {
        //Todo refactor to avoid code duplication
        String volumeKey = "volume";
        String layerOrderKey = "layerOrder";
        String tempoKey = "tempo";
        String vidTransKey = "videoTransparency";
        String vidState = "videoState";
        String visibleKey = "visible";
        String xKey = "x";
        String yKey = "y";
        String sizeKey = "size";
        String directionKey = "direction";
        String dragKey = "draggable";
        //String ttSLang = "textToSpeechLanguage"; // Ignored as this is an extension

        StringExpr keyExpr;
        double jsonDouble;
        String jsonString;
        boolean jsonBool;
        NumExpr numExpr;
        StringExpr stringExpr;
        BoolExpr boolExpr;
        SetStmt setStmt;

        List<SetStmt> list = new LinkedList<>();

        keyExpr = new Str(volumeKey);
        jsonDouble = scriptGroupNode.get(volumeKey).asDouble();
        numExpr = new Number((float) jsonDouble);
        setStmt = new SetAttributeTo(keyExpr, numExpr);
        list.add(setStmt);

        keyExpr = new Str(layerOrderKey);
        jsonDouble = scriptGroupNode.get(layerOrderKey).asDouble();
        numExpr = new Number((float) jsonDouble);
        setStmt = new SetAttributeTo(keyExpr, numExpr);
        list.add(setStmt);

        if (scriptGroupNode.get("isStage").asBoolean()) {
            //Stage

            keyExpr = new Str(tempoKey);
            jsonDouble = scriptGroupNode.get(tempoKey).asDouble();
            numExpr = new Number((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr);
            list.add(setStmt);

            keyExpr = new Str(vidTransKey);
            jsonDouble = scriptGroupNode.get(vidTransKey).asDouble();
            numExpr = new Number((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr);
            list.add(setStmt);

            keyExpr = new Str(vidState);
            jsonString = scriptGroupNode.get(vidTransKey).asText();
            stringExpr = new Str(jsonString);
            setStmt = new SetAttributeTo(keyExpr, stringExpr);
            list.add(setStmt);

        } else {

            keyExpr = new Str(visibleKey);
            jsonBool = scriptGroupNode.get(visibleKey).asBoolean();
            boolExpr = new Bool(jsonBool);
            setStmt = new SetAttributeTo(keyExpr, boolExpr);
            list.add(setStmt);

            keyExpr = new Str(xKey);
            jsonDouble = scriptGroupNode.get(xKey).asDouble();
            numExpr = new Number((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr);
            list.add(setStmt);

            keyExpr = new Str(yKey);
            jsonDouble = scriptGroupNode.get(yKey).asDouble();
            numExpr = new Number((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr);
            list.add(setStmt);

            keyExpr = new Str(sizeKey);
            jsonDouble = scriptGroupNode.get(sizeKey).asDouble();
            numExpr = new Number((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr);
            list.add(setStmt);

            keyExpr = new Str(directionKey);
            jsonDouble = scriptGroupNode.get(directionKey).asDouble();
            numExpr = new Number((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr);
            list.add(setStmt);

            keyExpr = new Str(dragKey);
            jsonBool = scriptGroupNode.get(dragKey).asBoolean();
            boolExpr = new Bool(jsonBool);
            setStmt = new SetAttributeTo(keyExpr, boolExpr);
            list.add(setStmt);

            throw new RuntimeException("rotationStyle not implemented");
        }

        return new SetStmtList(list);
    }

}

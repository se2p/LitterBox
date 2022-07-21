package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSet;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition.PositionType;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort.PortType;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.LEDMatrixStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.PortStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFace;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFacePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveStop;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.ChangeVolumeBy2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SetVolumeTo2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SpeakerStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.StopAllSounds2;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity.MEDIUM;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.*;

public class ParallelResourceUse extends AbstractRobotFinder {

    private static final String NAME = "parallel_resource_use";
    private final Map<Script, Stmt> motorsUseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix1UseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix2UseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix3UseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix4UseNode = new HashMap<>();
    private final Map<Script, Stmt> rockyLightUseNode = new HashMap<>();
    private final Map<Script, Stmt> led1UseNode = new HashMap<>();
    private final Map<Script, Stmt> led2UseNode = new HashMap<>();
    private final Map<Script, Stmt> soundUseNode = new HashMap<>();

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(ActorDefinition node) {
        ignoreLooseBlocks = true;
        Preconditions.checkNotNull(program);
        currentActor = node;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        String actorName = node.getIdent().getName();
        robot = getRobot(actorName, node.getSetStmtList());
        if (!(robot == CODEY || robot == MCORE)) {
            finishRobotVisit();
            resetMaps();
            return;
        }
        visitChildren(node);
        detectAllIssues();
        resetMaps();
    }

    @Override
    public void visit(RobotMoveStmt node) {
        boolean zeroPower = isZeroPower(node);
        if (!zeroPower && !(node instanceof MoveStop)) {
            addEntry(node, motorsUseNode);
        }
    }

    @Override
    public void visit(LEDMatrixStmt node) {
        if (node instanceof TurnOffFace || node instanceof TurnOffFacePort) {
            return;
        }
        if (node instanceof PortStmt) {
            PortType port = ((PortStmt) node).getPort().getPortType();
            switch (port) {
                default:
                case PORT_1:
                    addEntry(node, matrix1UseNode);
                    break;

                case PORT_2:
                    addEntry(node, matrix2UseNode);
                    break;

                case PORT_3:
                    addEntry(node, matrix3UseNode);
                    break;

                case PORT_4:
                    addEntry(node, matrix4UseNode);
                    break;
            }
        } else {
            addEntry(node, matrix1UseNode);
        }
    }

    @Override
    public void visit(EmotionStmt node) {
        addEntry(node, matrix1UseNode);
        if (node instanceof MovingEmotion) {
            addEntry(node, motorsUseNode);
        }
    }

    @Override
    public void visit(RockyLight node) {
        addEntry(node, rockyLightUseNode);
    }

    @Override
    public void visit(LEDStmt node) {
        boolean black = isBlack(node);
        if (black || node instanceof LEDOff) {
            return;
        }
        if (node instanceof PositionStmt) {
            PositionType position = ((PositionStmt) node).getPosition().getPositionType();
            switch (position) {
                case ALL:
                    addEntry(node, led1UseNode);
                    addEntry(node, led2UseNode);
                    break;

                case RIGHT:
                    addEntry(node, led2UseNode);
                    break;

                case LEFT:
                default:
                    addEntry(node, led1UseNode);
                    break;
            }
        } else {
            addEntry(node, led1UseNode);
        }
    }

    @Override
    public void visit(SpeakerStmt node) {
        if (!(node instanceof ChangeVolumeBy2 || node instanceof SetVolumeTo2 || node instanceof StopAllSounds2)) {
            addEntry(node, soundUseNode);
        }
    }

    private void addEntry(Stmt node, Map<Script, Stmt> map) {
        if (!map.containsKey(currentScript)) {
            map.put(currentScript, node);
        }
    }

    private void detectAllIssues() {
        detectIssue(motorsUseNode);
        detectIssue(matrix1UseNode);
        detectIssue(matrix2UseNode);
        detectIssue(matrix3UseNode);
        detectIssue(matrix4UseNode);
        detectIssue(rockyLightUseNode);
        detectIssue(led1UseNode);
        detectIssue(led2UseNode);
        detectIssue(soundUseNode);
    }

    private void detectIssue(Map<Script, Stmt> map) {
        List<Script> scriptList = new LinkedList<>(map.keySet());
        List<ASTNode> nodeList = new LinkedList<>(map.values());
        List<Metadata> metadataList = new LinkedList<>();
        nodeList.forEach((node) -> {
            metadataList.add(node.getMetadata());
        });
        if (nodeList.size() >= 2) {
            IssueSet issue = new IssueSet(this, MEDIUM, program, List.of(currentActor), scriptList, nodeList,
                    metadataList, new Hint(getName()));
            addIssue(issue);
        }
    }

    private void resetMaps() {
        motorsUseNode.clear();
        matrix1UseNode.clear();
        matrix2UseNode.clear();
        matrix3UseNode.clear();
        matrix4UseNode.clear();
        rockyLightUseNode.clear();
        led1UseNode.clear();
        led2UseNode.clear();
        soundUseNode.clear();
    }

    private boolean isBlack(LEDStmt node) {
        if (node instanceof ColorStmt) {
            StringExpr stringExpr = ((ColorStmt) node).getColorString();
            if (stringExpr instanceof StringLiteral) {
                String string = ((StringLiteral) stringExpr).getText();
                return string.equals("#000000");
            }
        } else if (node instanceof RGBValuesPosition) {
            NumValueVisitor calc = new NumValueVisitor();
            try {
                double red = calc.calculateEndValue(((RGBValuesPosition) node).getRed());
                double green = calc.calculateEndValue(((RGBValuesPosition) node).getGreen());
                double blue = calc.calculateEndValue(((RGBValuesPosition) node).getBlue());
                return red == 0 && green == 0 && blue == 0;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean isZeroPower(RobotMoveStmt node) {
        NumValueVisitor calc = new NumValueVisitor();
        try {
            if (node instanceof MoveDirection) {
                return 0 == calc.calculateEndValue(((MoveDirection) node).getPercent());
            } else if (node instanceof MoveSides) {
                double left = calc.calculateEndValue(((MoveSides) node).getLeftPower());
                double right = calc.calculateEndValue(((MoveSides) node).getRightPower());
                return left == 0 && right == 0;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.IRMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.MBlockStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.IRStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.LearnWithTime;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendIR;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendLearnResult;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetAxis;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetTimer2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

public interface MBlockVisitor extends ScratchVisitor {

    // MBlockNode

    /**
     * Default implementation of visit method for {@link MBlockNode}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockNode Node of which the children will
     *             be iterated
     */
    void visit(MBlockNode node);

    // MBlockEvent

    /**
     * Default implementation of visit method for {@link MBlockEvent}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockEvent Node of which the children will
     *             be iterated
     */
    default void visit(MBlockEvent node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link BoardButtonAction}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoardButtonAction Node of which the children will
     *             be iterated
     */
    default void visit(BoardButtonAction node) {
        visit((MBlockEvent) node);
    }

    /**
     * Default implementation of visit method for {@link BoardLaunch}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoardLaunch Node of which the children will
     *             be iterated
     */
    default void visit(BoardLaunch node) {
        visit((MBlockEvent) node);
    }

    /**
     * Default implementation of visit method for {@link BoardShaken}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoardShaken Node of which the children will
     *             be iterated
     */
    default void visit(BoardShaken node) {
        visit((MBlockEvent) node);
    }

    /**
     * Default implementation of visit method for {@link BoardTilted}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoardTilted Node of which the children will
     *             be iterated
     */
    default void visit(BoardTilted node) {
        visit((MBlockEvent) node);
    }

    /**
     * Default implementation of visit method for {@link BrightnessLess}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BrightnessLess Node of which the children will
     *             be iterated
     */
    default void visit(BrightnessLess node) {
        visit((MBlockEvent) node);
    }

    /**
     * Default implementation of visit method for {@link LaunchButton}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LaunchButton Node of which the children will
     *             be iterated
     */
    default void visit(LaunchButton node) {
        visit((MBlockEvent) node);
    }

    // MBlockBoolExpr

    /**
     * Default implementation of visit method for {@link MBlockBoolExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockBoolExpr Node of which the children will be iterated
     */
    default void visit(MBlockBoolExpr node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link BoardButtonPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoardButtonPressed Node of which the children will be iterated
     */
    default void visit(BoardButtonPressed node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ConnectRobot}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ConnectRobot Node of which the children will be iterated
     */
    default void visit(ConnectRobot node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link IRButtonPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IRButtonPressed Node of which the children will be iterated
     */
    default void visit(IRButtonPressed node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link LEDMatrixPosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDMatrixPosition Node of which the children will be iterated
     */
    default void visit(LEDMatrixPosition node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ObstaclesAhead}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ObstaclesAhead Node of which the children will be iterated
     */
    default void visit(ObstaclesAhead node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link OrientateTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node OrientateTo Node of which the children will be iterated
     */
    default void visit(OrientateTo node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link PortOnLine}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PortOnLine Node of which the children will be iterated
     */
    default void visit(PortOnLine node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RobotButtonPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotButtonPressed Node of which the children will be iterated
     */
    default void visit(RobotButtonPressed node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RobotShaken}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotShaken Node of which the children will be iterated
     */
    default void visit(RobotShaken node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RobotTilted}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotTilted Node of which the children will be iterated
     */
    default void visit(RobotTilted node) {
        visit((MBlockBoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link SeeColor}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SeeColor Node of which the children will be iterated
     */
    default void visit(SeeColor node) {
        visit((MBlockBoolExpr) node);
    }

    // MBlockNumExpr

    /**
     * Default implementation of visit method for {@link MBlockNumExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockNumExpr Node of which the children will be iterated
     */
    default void visit(MBlockNumExpr node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AmbientLight}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AmbientLight Node of which the children will be iterated
     */
    default void visit(AmbientLight node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link BatteryEnergy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BatteryEnergy Node of which the children will be iterated
     */
    default void visit(BatteryEnergy node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectAmbientLight}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectAmbientLight Node of which the children will be iterated
     */
    default void visit(DetectAmbientLight node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectAmbientLightPort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectAmbientLightPort Node of which the children will be iterated
     */
    default void visit(DetectAmbientLightPort node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectDistancePort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectDistancePort Node of which the children will be iterated
     */
    default void visit(DetectDistancePort node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectGrey}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectGrey Node of which the children will be iterated
     */
    default void visit(DetectGrey node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectIRReflection}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectIRReflection Node of which the children will be iterated
     */
    default void visit(DetectIRReflection node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectLinePort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectLinePort Node of which the children will be iterated
     */
    default void visit(DetectLinePort node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectReflection}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectReflection Node of which the children will be iterated
     */
    default void visit(DetectReflection node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DetectRGBValue}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectRGBValue Node of which the children will be iterated
     */
    default void visit(DetectRGBValue node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link GyroPitchAngle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GyroPitchAngle Node of which the children will be iterated
     */
    default void visit(GyroPitchAngle node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link GyroRollAngle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GyroRollAngle Node of which the children will be iterated
     */
    default void visit(GyroRollAngle node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Potentiometer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Potentiometer Node of which the children will be iterated
     */
    default void visit(Potentiometer node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RobotTimer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotTimer Node of which the children will be iterated
     */
    default void visit(RobotTimer node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RotateXAngle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RotateXAngle Node of which the children will be iterated
     */
    default void visit(RotateXAngle node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RotateYAngle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RotateYAngle Node of which the children will be iterated
     */
    default void visit(RotateYAngle node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link RotateZAngle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RotateZAngle Node of which the children will be iterated
     */
    default void visit(RotateZAngle node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ShakingStrength}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShakingStrength Node of which the children will be iterated
     */
    default void visit(ShakingStrength node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link SoundVolume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundVolume Node of which the children will be iterated
     */
    default void visit(SoundVolume node) {
        visit((MBlockNumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link SpeakerVolume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpeakerVolume Node of which the children will be iterated
     */
    default void visit(SpeakerVolume node) {
        visit((MBlockNumExpr) node);
    }

    // MBlock-String-Expr

    /**
     * Default implementation of visit method for {@link MBlockStringExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockStringExpr Node of which the children will be iterated
     */
    default void visit(MBlockStringExpr node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link IRMessage}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IRMessage Node of which the children will be iterated
     */
    default void visit(IRMessage node) {
        visit((MBlockStringExpr) node);
    }

    // MBlockOption

    /**
     * Default implementation of visit method for {@link MBlockOption}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockOption Node of which the children will be iterated
     */
    default void visit(MBlockOption node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link BlackWhite}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BlackWhite Node of which the children will be iterated
     */
    default void visit(BlackWhite node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link IRRemoteButton}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IRRemoteButton Node of which the children will be iterated
     */
    default void visit(IRRemoteButton node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link LEDColor}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDColor Node of which the children will
     *             be iterated
     */
    default void visit(LEDColor node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link LEDMatrix}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDMatrix Node of which the children will
     *             be iterated
     */
    default void visit(LEDMatrix node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link LEDPosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDPosition Node of which the children will
     *             be iterated
     */
    default void visit(LEDPosition node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link LineFollowState}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LineFollowState Node of which the children will
     *             be iterated
     */
    default void visit(LineFollowState node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link MCorePort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MCorePort Node of which the children will
     *             be iterated
     */
    default void visit(MCorePort node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link PadOrientation}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PadOrientation Node of which the children will be iterated
     */
    default void visit(PadOrientation node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link PressedState}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PressedState  Node of which the children will be iterated
     */
    default void visit(PressedState node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link RGB}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RGB Node of which the children will
     *             be iterated
     */
    default void visit(RGB node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link RobotAxis}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotAxis Node of which the children will
     *             be iterated
     */
    default void visit(RobotAxis node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link RobotButton}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotButton  Node of which the children will be iterated
     */
    default void visit(RobotButton node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link RobotDirection}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node EventDirection  Node of which the children will be iterated
     */
    default void visit(RobotDirection node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link SoundList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundList Node of which the children will
     *             be iterated
     */
    default void visit(SoundList node) {
        visit((MBlockOption) node);
    }

    /**
     * Default implementation of visit method for {@link SoundNote}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundNote Node of which the children will
     *             be iterated
     */
    default void visit(SoundNote node) {
        visit((MBlockOption) node);
    }

    // MBlockStmt

    /**
     * Default implementation of visit method for {@link MBlockStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MBlockStmt Node of which the children will
     *             be iterated
     */
    default void visit(MBlockStmt node) {
        visit((Stmt) node);
    }

    // EmotionStmt

    /**
     * Default implementation of visit method for {@link EmotionStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node EmotionStmt Node of which the children will
     *             be iterated
     */
    default void visit(EmotionStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link MovingEmotion}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MovingEmotion Node of which the children will
     *             be iterated
     */
    default void visit(MovingEmotion node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Aggrieved}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Aggrieved Node of which the children will
     *             be iterated
     */
    default void visit(Aggrieved node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Agree}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Agree Node of which the children will
     *             be iterated
     */
    default void visit(Agree node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Angry}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Angry Node of which the children will
     *             be iterated
     */
    default void visit(Angry node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Awkward}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Awkward Node of which the children will
     *             be iterated
     */
    default void visit(Awkward node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Coquetry}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Coquetry Node of which the children will
     *             be iterated
     */
    default void visit(Coquetry node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Deny}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Deny Node of which the children will
     *             be iterated
     */
    default void visit(Deny node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Dizzy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Dizzy Node of which the children will
     *             be iterated
     */
    default void visit(Dizzy node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Exclaim}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Exclaim Node of which the children will
     *             be iterated
     */
    default void visit(Exclaim node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Greeting}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Greeting Node of which the children will
     *             be iterated
     */
    default void visit(Greeting node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link LookAround}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LookAround Node of which the children will
     *             be iterated
     */
    default void visit(LookAround node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link LookDown}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LookDown Node of which the children will
     *             be iterated
     */
    default void visit(LookDown node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LookLeft}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LookLeft Node of which the children will
     *             be iterated
     */
    default void visit(LookLeft node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LookRight}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LookRight Node of which the children will
     *             be iterated
     */
    default void visit(LookRight node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LookUp}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LookUp Node of which the children will
     *             be iterated
     */
    default void visit(LookUp node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Naughty}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Naughty Node of which the children will
     *             be iterated
     */
    default void visit(Naughty node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Proud}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Proud Node of which the children will
     *             be iterated
     */
    default void visit(Proud node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Revive}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Revive Node of which the children will
     *             be iterated
     */
    default void visit(Revive node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Sad}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Sad Node of which the children will
     *             be iterated
     */
    default void visit(Sad node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Shiver}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Shiver Node of which the children will
     *             be iterated
     */
    default void visit(Shiver node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Sleeping}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Sleeping Node of which the children will
     *             be iterated
     */
    default void visit(Sleeping node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Sleepy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Sleepy Node of which the children will
     *             be iterated
     */
    default void visit(Sleepy node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Smile}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Smile Node of which the children will
     *             be iterated
     */
    default void visit(Smile node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Sprint}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Sprint Node of which the children will
     *             be iterated
     */
    default void visit(Sprint node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Startle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Startle Node of which the children will
     *             be iterated
     */
    default void visit(Startle node) {
        visit((MovingEmotion) node);
    }

    /**
     * Default implementation of visit method for {@link Wink}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Wink Node of which the children will
     *             be iterated
     */
    default void visit(Wink node) {
        visit((EmotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Yeah}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Yeah Node of which the children will
     *             be iterated
     */
    default void visit(Yeah node) {
        visit((MovingEmotion) node);
    }

    // IRStmt

    /**
     * Default implementation of visit method for {@link IRStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IRStmt Node of which the children will
     *             be iterated
     */
    default void visit(IRStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LearnWithTime}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LearnWithTime Node of which the children will
     *             be iterated
     */
    default void visit(LearnWithTime node) {
        visit((IRStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SendIR}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SendIR Node of which the children will
     *             be iterated
     */
    default void visit(SendIR node) {
        visit((IRStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SendLearnResult}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SendLearnResult Node of which the children will
     *             be iterated
     */
    default void visit(SendLearnResult node) {
        visit((IRStmt) node);
    }

    // LEDStmt

    /**
     * Default implementation of visit method for {@link LEDStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDStmt Node of which the children will
     *             be iterated
     */
    default void visit(LEDStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDColorShow}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDColorShow Node of which the children will
     *             be iterated
     */
    default void visit(LEDColorShow node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDColorShowPosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDColorShowPosition Node of which the children will
     *             be iterated
     */
    default void visit(LEDColorShowPosition node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDColorTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDColorTimed Node of which the children will
     *             be iterated
     */
    default void visit(LEDColorTimed node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDColorTimedPosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDColorTimedPosition Node of which the children will
     *             be iterated
     */
    default void visit(LEDColorTimedPosition node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDOff}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDOff Node of which the children will
     *             be iterated
     */
    default void visit(LEDOff node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link RGBValue}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectRGBValue Node of which the children will
     *             be iterated
     */
    default void visit(RGBValue node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link RGBValuesPosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DetectRGBValuesPosition Node of which the children will
     *             be iterated
     */
    default void visit(RGBValuesPosition node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link RockyLight}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RockyLight Node of which the children will
     *             be iterated
     */
    default void visit(RockyLight node) {
        visit((LEDStmt) node);
    }

    /**
     * Default implementation of visit method for {@link RockyLightOff}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RockyLightOff Node of which the children will
     *             be iterated
     */
    default void visit(RockyLightOff node) {
        visit((LEDStmt) node);
    }

    // LEDMatrixStmt

    /**
     * Default implementation of visit method for {@link LEDMatrixStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDMatrixStmt Node of which the children will
     *             be iterated
     */
    default void visit(LEDMatrixStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link FacePosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FacePosition Node of which the children will
     *             be iterated
     */
    default void visit(FacePosition node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link FacePositionPort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FacePositionPort Node of which the children will
     *             be iterated
     */
    default void visit(FacePositionPort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link FaceTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FaceTimed Node of which the children will
     *             be iterated
     */
    default void visit(FaceTimed node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link FaceTimedPort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FaceTimedPort Node of which the children will
     *             be iterated
     */
    default void visit(FaceTimedPort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDNumPort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDNumPort Node of which the children will
     *             be iterated
     */
    default void visit(LEDNumPort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDString}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDString Node of which the children will
     *             be iterated
     */
    default void visit(LEDString node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDStringPort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDStringPort Node of which the children will
     *             be iterated
     */
    default void visit(LEDStringPort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDStringPosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDStringPosition Node of which the children will
     *             be iterated
     */
    default void visit(LEDStringPosition node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDStringPositionPort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDStringPositionPort Node of which the children will
     *             be iterated
     */
    default void visit(LEDStringPositionPort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDStringScrolling}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDStringScrolling Node of which the children will
     *             be iterated
     */
    default void visit(LEDStringScrolling node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDSwitchOff}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDSwitchOff Node of which the children will
     *             be iterated
     */
    default void visit(LEDSwitchOff node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDSwitchOn}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDSwitchOn Node of which the children will
     *             be iterated
     */
    default void visit(LEDSwitchOn node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDTimePort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDTimePort Node of which the children will
     *             be iterated
     */
    default void visit(LEDTimePort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link LEDToggle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LEDToggle Node of which the children will
     *             be iterated
     */
    default void visit(LEDToggle node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ShowFace}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShowFace Node of which the children will
     *             be iterated
     */
    default void visit(ShowFace node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ShowFacePort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShowFacePort Node of which the children will
     *             be iterated
     */
    default void visit(ShowFacePort node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnOffFace}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnOffFace Node of which the children will
     *             be iterated
     */
    default void visit(TurnOffFace node) {
        visit((LEDMatrixStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnOffFacePort}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnOffFacePort Node of which the children will
     *             be iterated
     */
    default void visit(TurnOffFacePort node) {
        visit((LEDMatrixStmt) node);
    }

    // RobotMoveStmt

    /**
     * Default implementation of visit method for {@link RobotMoveStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RobotMoveStmt Node of which the children will
     *             be iterated
     */
    default void visit(RobotMoveStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link KeepBackwardTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node KeepBackwardTimed Node of which the children will
     *             be iterated
     */
    default void visit(KeepBackwardTimed node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link KeepForwardTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node KeepForwardTimed Node of which the children will
     *             be iterated
     */
    default void visit(KeepForwardTimed node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link MoveBackwardTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveBackwardTimed Node of which the children will
     *             be iterated
     */
    default void visit(MoveBackwardTimed node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link MoveDirection}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveDirection Node of which the children will
     *             be iterated
     */
    default void visit(MoveDirection node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link MoveForwardTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveForwardTimed Node of which the children will
     *             be iterated
     */
    default void visit(MoveForwardTimed node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link MoveSides}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveSides Node of which the children will
     *             be iterated
     */
    default void visit(MoveSides node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link MoveStop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveStop Node of which the children will
     *             be iterated
     */
    default void visit(MoveStop node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnLeft2}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnLeft2 Node of which the children will
     *             be iterated
     */
    default void visit(TurnLeft2 node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnLeftTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnLeftTimed Node of which the children will
     *             be iterated
     */
    default void visit(TurnLeftTimed node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnRight2}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnRight2 Node of which the children will
     *             be iterated
     */
    default void visit(TurnRight2 node) {
        visit((RobotMoveStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnRightTimed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnRightTimed Node of which the children will
     *             be iterated
     */
    default void visit(TurnRightTimed node) {
        visit((RobotMoveStmt) node);
    }

    // ResetStmt

    /**
     * Default implementation of visit method for {@link ResetStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResetStmt Node of which the children will
     *             be iterated
     */
    default void visit(ResetStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ResetAxis}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResetAxis Node of which the children will
     *             be iterated
     */
    default void visit(ResetAxis node) {
        visit((ResetStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ResetTimer2}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResetTimer2 Node of which the children will
     *             be iterated
     */
    default void visit(ResetTimer2 node) {
        visit((ResetStmt) node);
    }

    // SpeakerStmt

    /**
     * Default implementation of visit method for {@link SpeakerStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpeakerStmt Node of which the children will
     *             be iterated
     */
    default void visit(SpeakerStmt node) {
        visit((MBlockStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeVolumeBy2}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeVolumeBy2 Node of which the children will
     *             be iterated
     */
    default void visit(ChangeVolumeBy2 node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Pause}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Pause Node of which the children will
     *             be iterated
     */
    default void visit(Pause node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PlayFrequency}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PlayFrequency Node of which the children will
     *             be iterated
     */
    default void visit(PlayFrequency node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PlayNote}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PlayNote Node of which the children will
     *             be iterated
     */
    default void visit(PlayNote node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PlaySound}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PlaySound Node of which the children will
     *             be iterated
     */
    default void visit(PlaySound node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PlaySoundWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PlaySoundWait Node of which the children will
     *             be iterated
     */
    default void visit(PlaySoundWait node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetVolumeTo2}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetVolumeTo2 Node of which the children will
     *             be iterated
     */
    default void visit(SetVolumeTo2 node) {
        visit((SpeakerStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StopAllSounds2}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopAllSounds2 Node of which the children will
     *             be iterated
     */
    default void visit(StopAllSounds2 node) {
        visit((SpeakerStmt) node);
    }
}

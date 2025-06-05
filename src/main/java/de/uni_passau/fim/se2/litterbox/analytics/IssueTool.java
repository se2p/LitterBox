/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.*;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.Timer;
import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.*;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes.*;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.smells.MotorPowerMinus;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.smells.MultiAttributeModificationRobot;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.smells.UnnecessaryTimeRobot;
import de.uni_passau.fim.se2.litterbox.analytics.questions.*;
import de.uni_passau.fim.se2.litterbox.analytics.smells.*;
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private static final Logger log = Logger.getLogger(IssueTool.class.getName());
    private static final boolean LOAD_GENERAL = PropertyLoader.getSystemBooleanProperty("issues.load_general");
    private static final boolean LOAD_MBLOCK = PropertyLoader.getSystemBooleanProperty("issues.load_mblock");

    private static Map<String, IssueFinder> generateBugFinders() {
        Map<String, IssueFinder> bugFinders = new LinkedHashMap<>();
        if (LOAD_GENERAL) {
            registerBugFinder(new AmbiguousCustomBlockSignature(), bugFinders);
            registerBugFinder(new AmbiguousParameterNameUsed(), bugFinders);
            registerBugFinder(new BlockingIfElse(), bugFinders);
            registerBugFinder(new CallWithoutDefinition(), bugFinders);
            registerBugFinder(new ComparingLiterals(), bugFinders);
            registerBugFinder(new CustomBlockWithForever(), bugFinders);
            registerBugFinder(new CustomBlockWithTermination(), bugFinders);
            registerBugFinder(new EndlessRecursion(), bugFinders);
            registerBugFinder(new ExpressionAsTouchingOrColor(), bugFinders);
            registerBugFinder(new ForeverInsideIf(), bugFinders);
            registerBugFinder(new ForeverInsideLoop(), bugFinders);
            registerBugFinder(new HideWithoutShow(), bugFinders);
            registerBugFinder(new IllegalParameterRefactor(), bugFinders);
            registerBugFinder(new ImmediateDeleteCloneAfterBroadcast(), bugFinders);
            registerBugFinder(new ImmediateStopAfterSay(), bugFinders);
            registerBugFinder(new InappropriateHandlerDeleteClone(), bugFinders);
            registerBugFinder(new InterruptedLoopSensing(), bugFinders);
            registerBugFinder(new KeySetPosition(), bugFinders);
            registerBugFinder(new MessageNeverReceived(), bugFinders);
            registerBugFinder(new MessageNeverSent(), bugFinders);
            registerBugFinder(new MissingAsk(), bugFinders);
            registerBugFinder(new MissingBackdropSwitch(), bugFinders);
            registerBugFinder(new MissingCloneCall(), bugFinders);
            registerBugFinder(new MissingCloneInitialization(), bugFinders);
            registerBugFinder(new MissingEraseAll(), bugFinders);
            registerBugFinder(new MissingInitialization(), bugFinders);
            registerBugFinder(new MissingLoopMousePosition(), bugFinders);
            registerBugFinder(new MissingLoopSensing(), bugFinders);
            registerBugFinder(new MissingPenDown(), bugFinders);
            registerBugFinder(new MissingPenUp(), bugFinders);
            registerBugFinder(new MissingResource(), bugFinders);
            registerBugFinder(new MissingTerminationCondition(), bugFinders);
            registerBugFinder(new MissingWaitUntilCondition(), bugFinders);
            registerBugFinder(new NoWorkingScripts(), bugFinders);
            registerBugFinder(new OrphanedParameter(), bugFinders);
            registerBugFinder(new ParameterOutOfScope(), bugFinders);
            registerBugFinder(new PositionEqualsCheck(), bugFinders);
            registerBugFinder(new RecursiveCloning(), bugFinders);
            registerBugFinder(new TerminatedLoop(), bugFinders);
            registerBugFinder(new TypeError(), bugFinders);
            registerBugFinder(new VariableAsLiteral(), bugFinders);
        }
        if (LOAD_MBLOCK) { // mBlock bugs
            registerBugFinder(new AmbientLightOutOfBounds(), bugFinders);
            registerBugFinder(new BatteryLevelOutOfBounds(), bugFinders);
            registerBugFinder(new CodeyUploadStopTimed(), bugFinders);
            registerBugFinder(new ColorDetectionOutOfBounds(), bugFinders);
            registerBugFinder(new ColorSettingOutOfBounds(), bugFinders);
            registerBugFinder(new DetectRepeatInLoop(), bugFinders);
            registerBugFinder(new GearPotentiometerOutOfBounds(), bugFinders);
            registerBugFinder(new InterruptedLoopSensingRobot(), bugFinders);
            registerBugFinder(new LEDOffScriptMissing(), bugFinders);
            registerBugFinder(new LineFollowingOutOfBounds(), bugFinders);
            registerBugFinder(new LoopedStatementNotStopped(), bugFinders);
            registerBugFinder(new LoudnessOutOfBounds(), bugFinders);
            registerBugFinder(new MatrixOffScriptMissing(), bugFinders);
            registerBugFinder(new MissingLoopRobotSensing(), bugFinders);
            registerBugFinder(new MotorLowPower(), bugFinders);
            registerBugFinder(new MotorPowerOutOfBounds(), bugFinders);
            registerBugFinder(new MotorStopScriptMissing(), bugFinders);
            registerBugFinder(new ParallelBoardLaunchScriptMCore(), bugFinders);
            registerBugFinder(new ParallelResourceUse(), bugFinders);
            registerBugFinder(new PitchAngleOutOfBounds(), bugFinders);
            registerBugFinder(new RockyLightOffScriptMissing(), bugFinders);
            registerBugFinder(new RollAngleOutOfBounds(), bugFinders);
            registerBugFinder(new SensorValueEquals(), bugFinders);
            registerBugFinder(new ShakingStrengthOutOfBounds(), bugFinders);
            registerBugFinder(new TimedStatementInLiveLoop(), bugFinders);
            registerBugFinder(new UltraSonicOutOfBounds(), bugFinders);
        }

        return bugFinders;
    }

    /**
     * Bug finders that can operate on single scripts, i.e. do not need other information from somewhere else in the
     * program.
     *
     * @return Issue finders that work on single scripts.
     */
    private static Map<String, IssueFinder> generateScriptsBugFinders() {
        Map<String, IssueFinder> bugFinders = new LinkedHashMap<>();

        registerBugFinder(new AmbiguousParameterNameUsed(), bugFinders);
        registerBugFinder(new BlockingIfElse(), bugFinders);
        registerBugFinder(new ComparingLiterals(), bugFinders);
        registerBugFinder(new EndlessRecursion(), bugFinders);
        registerBugFinder(new ExpressionAsTouchingOrColor(), bugFinders);
        registerBugFinder(new ForeverInsideIf(), bugFinders);
        registerBugFinder(new ForeverInsideLoop(), bugFinders);
        registerBugFinder(new IllegalParameterRefactor(), bugFinders);
        registerBugFinder(new ImmediateDeleteCloneAfterBroadcast(), bugFinders);
        registerBugFinder(new ImmediateStopAfterSay(), bugFinders);
        registerBugFinder(new InappropriateHandlerDeleteClone(), bugFinders);
        registerBugFinder(new InterruptedLoopSensing(), bugFinders);
        registerBugFinder(new KeySetPosition(), bugFinders);
        registerBugFinder(new MissingLoopMousePosition(), bugFinders);
        registerBugFinder(new MissingLoopSensing(), bugFinders);
        registerBugFinder(new MissingTerminationCondition(), bugFinders);
        registerBugFinder(new MissingWaitUntilCondition(), bugFinders);
        registerBugFinder(new OrphanedParameter(), bugFinders);
        registerBugFinder(new ParameterOutOfScope(), bugFinders);
        registerBugFinder(new PositionEqualsCheck(), bugFinders);
        registerBugFinder(new RecursiveCloning(), bugFinders);
        registerBugFinder(new TerminatedLoop(), bugFinders);
        registerBugFinder(new TypeError(), bugFinders);
        registerBugFinder(new VariableAsLiteral(), bugFinders);

        return bugFinders;
    }

    private static Map<String, IssueFinder> generateAllFinders() {
        Map<String, IssueFinder> allFinders = new LinkedHashMap<>(generateBugFinders());
        allFinders.putAll(generateSmellFinders());
        allFinders.putAll(generatePerfumeFinders());
        allFinders.putAll(generateQuestionFinders());

        return allFinders;
    }

    private static Map<String, IssueFinder> generateSmellFinders() {
        Map<String, IssueFinder> smellFinders = new LinkedHashMap<>();

        // Smells
        if (LOAD_GENERAL) {
            registerSmellFinder(new AmbiguousParameterNameUnused(), smellFinders);
            registerSmellFinder(new BusyWaiting(), smellFinders);
            registerSmellFinder(new ClonedCodeType1(), smellFinders);
            registerSmellFinder(new ClonedCodeType2(), smellFinders);
            registerSmellFinder(new ClonedCodeType3(), smellFinders);
            registerSmellFinder(new DeadCode(), smellFinders);
            registerSmellFinder(new DeleteCloneInLoop(), smellFinders);
            registerSmellFinder(new DoubleIf(), smellFinders);
            registerSmellFinder(new DuplicatedScript(), smellFinders);
            registerSmellFinder(new DuplicatedScriptsCovering(), smellFinders);
            registerSmellFinder(new DuplicateSprite(), smellFinders);
            registerSmellFinder(new EmptyControlBody(), smellFinders);
            registerSmellFinder(new EmptyCustomBlock(), smellFinders);
            registerSmellFinder(new EmptyProject(), smellFinders);
            registerSmellFinder(new EmptyScript(), smellFinders);
            registerSmellFinder(new EmptySprite(), smellFinders);
            registerSmellFinder(new LongScript(), smellFinders);
            registerSmellFinder(new MessageNaming(), smellFinders);
            registerSmellFinder(new MiddleMan(), smellFinders);
            registerSmellFinder(new MultiAttributeModification(), smellFinders);
            registerSmellFinder(new NestedLoops(), smellFinders);
            registerSmellFinder(new SameVariableDifferentSprite(), smellFinders);
            registerSmellFinder(new SequentialActions(), smellFinders);
            registerSmellFinder(new SpriteNaming(), smellFinders);
            registerSmellFinder(new StutteringMovement(), smellFinders);
            registerSmellFinder(new UnnecessaryBoolean(), smellFinders);
            registerSmellFinder(new UnnecessaryIf(), smellFinders);
            registerSmellFinder(new UnnecessaryIfAfterUntil(), smellFinders);
            registerSmellFinder(new UnnecessaryLoop(), smellFinders);
            registerSmellFinder(new UnnecessaryMessage(), smellFinders);
            registerSmellFinder(new UnnecessaryMove(), smellFinders);
            registerSmellFinder(new UnnecessaryRotation(), smellFinders);
            registerSmellFinder(new UnnecessarySizeChange(), smellFinders);
            registerSmellFinder(new UnnecessaryStopScript(), smellFinders);
            registerSmellFinder(new UnnecessaryTime(), smellFinders);
            registerSmellFinder(new UnusedCustomBlock(), smellFinders);
            registerSmellFinder(new UnusedParameter(), smellFinders);
            registerSmellFinder(new UnusedVariable(), smellFinders);
            registerSmellFinder(new UselessBlocks(), smellFinders);
            registerSmellFinder(new UselessWait(), smellFinders);
            registerSmellFinder(new VariableInitializationRace(), smellFinders);
        }
        if (LOAD_MBLOCK) { // mBlock smells
            registerSmellFinder(new MotorPowerMinus(), smellFinders);
            registerSmellFinder(new MultiAttributeModificationRobot(), smellFinders);
            registerSmellFinder(new UnnecessaryTimeRobot(), smellFinders);
        }

        return smellFinders;
    }

    public static Map<String, IssueFinder> generatePerfumeFinders() {
        Map<String, IssueFinder> perfumeFinders = new LinkedHashMap<>();

        if (LOAD_GENERAL) {
            registerPerfumeFinder(new BackdropSwitch(), perfumeFinders);
            registerPerfumeFinder(new BoolExpression(), perfumeFinders);
            registerPerfumeFinder(new Collision(), perfumeFinders);
            registerPerfumeFinder(new ConditionalInsideLoop(), perfumeFinders);
            registerPerfumeFinder(new ControlledBroadcastOrStop(), perfumeFinders);
            registerPerfumeFinder(new Coordination(), perfumeFinders);
            registerPerfumeFinder(new CorrectBroadcast(), perfumeFinders);
            registerPerfumeFinder(new CustomBlockUsage(), perfumeFinders);
            registerPerfumeFinder(new DirectedMotion(), perfumeFinders);
            registerPerfumeFinder(new GlidingMotion(), perfumeFinders);
            registerPerfumeFinder(new InitialisationOfLooks(), perfumeFinders);
            registerPerfumeFinder(new InitialisationOfPosition(), perfumeFinders);
            registerPerfumeFinder(new ListUsage(), perfumeFinders);
            registerPerfumeFinder(new LoopSensing(), perfumeFinders);
            registerPerfumeFinder(new MatchingParameter(), perfumeFinders);
            registerPerfumeFinder(new MouseFollower(), perfumeFinders);
            registerPerfumeFinder(new MovementInLoop(), perfumeFinders);
            registerPerfumeFinder(new NestedConditionalChecks(), perfumeFinders);
            registerPerfumeFinder(new NestedLoopsPerfume(), perfumeFinders);
            registerPerfumeFinder(new ObjectFollower(), perfumeFinders);
            registerPerfumeFinder(new Parallelisation(), perfumeFinders);
            registerPerfumeFinder(new SaySoundSynchronisation(), perfumeFinders);
            registerPerfumeFinder(new Timer(), perfumeFinders);
            registerPerfumeFinder(new UsedVariables(), perfumeFinders);
            registerPerfumeFinder(new UsefulPositionCheck(), perfumeFinders);
            registerPerfumeFinder(new ValidTerminationCondition(), perfumeFinders);
            registerPerfumeFinder(new WaitingCheckToStop(), perfumeFinders);
        }

        if (LOAD_MBLOCK) {
            registerPerfumeFinder(new AmbientLightInBounds(), perfumeFinders);
            registerPerfumeFinder(new BatteryLevelInBounds(), perfumeFinders);
            registerPerfumeFinder(new ColorDetectionInBounds(), perfumeFinders);
            registerPerfumeFinder(new ColorSettingInBounds(), perfumeFinders);
            registerPerfumeFinder(new GearPotentiometerInBounds(), perfumeFinders);
            registerPerfumeFinder(new LEDOffScript(), perfumeFinders);
            registerPerfumeFinder(new LineFollowingInBounds(), perfumeFinders);
            registerPerfumeFinder(new LoopSensingRobot(), perfumeFinders);
            registerPerfumeFinder(new LoudnessInBounds(), perfumeFinders);
            registerPerfumeFinder(new MatrixOffScript(), perfumeFinders);
            registerPerfumeFinder(new MotorOffScript(), perfumeFinders);
            registerPerfumeFinder(new MotorPowerInBounds(), perfumeFinders);
            registerPerfumeFinder(new ParallelisationRobot(), perfumeFinders);
            registerPerfumeFinder(new PitchAngleInBounds(), perfumeFinders);
            registerPerfumeFinder(new RockyLightOffScript(), perfumeFinders);
            registerPerfumeFinder(new RollAngleInBounds(), perfumeFinders);
            registerPerfumeFinder(new ShakingStrengthInBounds(), perfumeFinders);
            registerPerfumeFinder(new UltraSonicInBounds(), perfumeFinders);
        }

        return perfumeFinders;
    }

    public static Map<String, IssueFinder> generateQuestionFinders() {
        Map<String, IssueFinder> questionFinders = new LinkedHashMap<>();

        if (LOAD_GENERAL) {
            registerQuestionFinder(new BlockControllingLoop(), questionFinders);
            registerQuestionFinder(new DefinitionOfProcedure(), questionFinders);
            registerQuestionFinder(new ElementInLoopBody(), questionFinders);
            registerQuestionFinder(new ElementInLoopCondition(), questionFinders);
            registerQuestionFinder(new IfBlockCondition(), questionFinders);
            registerQuestionFinder(new IfElseStatementExecution(), questionFinders);
            registerQuestionFinder(new IfThenStatementExecution(), questionFinders);
            registerQuestionFinder(new PurposeOfBroadcast(), questionFinders);
            registerQuestionFinder(new PurposeOfForeverLoop(), questionFinders);
            registerQuestionFinder(new PurposeOfIfCondition(), questionFinders);
            registerQuestionFinder(new PurposeOfLoopCondition(), questionFinders);
            registerQuestionFinder(new PurposeOfProcedure(), questionFinders);
            registerQuestionFinder(new PurposeOfProgram(), questionFinders);
            registerQuestionFinder(new PurposeOfRepeatTimesLoop(), questionFinders);
            registerQuestionFinder(new PurposeOfRepeatUntilLoop(), questionFinders);
            registerQuestionFinder(new PurposeOfScript(), questionFinders);
            registerQuestionFinder(new PurposeOfVariable(), questionFinders);
            registerQuestionFinder(new RepeatTimesLiteralExecution(), questionFinders);
            registerQuestionFinder(new ScriptsForActor(), questionFinders);
            registerQuestionFinder(new ScriptsInProgram(), questionFinders);
            registerQuestionFinder(new ScriptsTriggeredByEvent(), questionFinders);
            registerQuestionFinder(new ScriptsTriggeredByStatement(), questionFinders);
            registerQuestionFinder(new ScriptExecutionOrderDifferentActors(), questionFinders);
            registerQuestionFinder(new ScriptExecutionOrderSameActor(), questionFinders);
            registerQuestionFinder(new ScriptToSetVariable(), questionFinders);
            registerQuestionFinder(new SetVariable(), questionFinders);
            registerQuestionFinder(new StatementsInIfStatement(), questionFinders);
            registerQuestionFinder(new StatementTriggersEvent(), questionFinders);
            registerQuestionFinder(new VariableForActor(), questionFinders);
            registerQuestionFinder(new VariableInScript(), questionFinders);
        }

        return questionFinders;
    }

    public static List<IssueFinder> getFinders(final FinderGroup finderGroup) {
        final Collection<IssueFinder> finders = switch (finderGroup) {
            case ALL -> generateAllFinders().values();
            case SMELLS -> generateSmellFinders().values();
            case BUGS -> generateBugFinders().values();
            case BUGS_SCRIPTS -> generateScriptsBugFinders().values();
            case PERFUMES -> generatePerfumeFinders().values();
            case QUESTIONS -> generateQuestionFinders().values();
            case DEFAULT -> generateAllFinders().values().stream()
                    .filter(f -> !f.getName().toLowerCase().endsWith("strict"))
                    .toList();
            case FLAWS -> {
                final Collection<IssueFinder> bugFinders = generateBugFinders().values();
                final Collection<IssueFinder> smellFinders = generateSmellFinders().values();
                yield Stream.concat(bugFinders.stream(), smellFinders.stream()).toList();
            }
            case MOST_COMMON_BUGS -> List.of(
                    new MessageNeverSent(),
                    new MissingLoopSensing(),
                    new ComparingLiterals(),
                    new MissingCloneInitialization(),
                    new MessageNeverReceived(),
                    new ForeverInsideLoop(),
                    new StutteringMovement()
            );
        };

        return List.copyOf(finders);
    }

    public static List<IssueFinder> getFinders(String commandString) {
        final Optional<FinderGroup> issueType = FinderGroup.tryFromString(commandString);
        if (issueType.isPresent()) {
            return getFinders(issueType.get());
        }

        List<IssueFinder> finders = new ArrayList<>();

        for (String detectorName : commandString.split(",")) {
            Map<String, IssueFinder> allFinders = generateAllFinders();
            if (!allFinders.containsKey(detectorName)) {
                // TODO: Hard crash might be more appropriate to notify user
                log.log(Level.SEVERE, "Unknown finder: " + detectorName);
                continue;
            }
            finders.add(allFinders.get(detectorName));
        }

        return Collections.unmodifiableList(finders);
    }

    public static Collection<String> getAllFinderNames() {
        return Collections.unmodifiableSet(generateAllFinders().keySet());
    }

    public static Collection<String> getBugFinderNames() {
        return Collections.unmodifiableSet(generateBugFinders().keySet());
    }

    public static Collection<String> getSmellFinderNames() {
        return Collections.unmodifiableSet(generateSmellFinders().keySet());
    }

    public static Collection<String> getPerfumeFinderNames() {
        return Collections.unmodifiableSet(generatePerfumeFinders().keySet());
    }

    public static Collection<String> getQuestionFinderNames() {
        return Collections.unmodifiableSet(generateQuestionFinders().keySet());
    }

    static void registerSmellFinder(IssueFinder finder, Map<String, IssueFinder> smellFinders) {
        if (finder.getIssueType() != IssueType.SMELL) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Smell IssueFinder");
        }

        smellFinders.put(finder.getName(), finder);
    }

    static void registerBugFinder(IssueFinder finder, Map<String, IssueFinder> bugFinders) {
        if (finder.getIssueType() != IssueType.BUG) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Bug IssueFinder");
        }
        bugFinders.put(finder.getName(), finder);
    }

    static void registerPerfumeFinder(IssueFinder finder, Map<String, IssueFinder> perfumeFinders) {
        if (finder.getIssueType() != IssueType.PERFUME) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Solution IssueFinder");
        }
        perfumeFinders.put(finder.getName(), finder);
    }

    static List<String> generateFixHeuristicsNames() {
        List<String> fixHeuristicsNames = new ArrayList<>();
        fixHeuristicsNames.add(ComparingLiteralsFix.NAME);
        fixHeuristicsNames.add(ForeverInsideLoopFix.NAME);
        fixHeuristicsNames.add(MessageNeverReceivedFix.NAME);
        fixHeuristicsNames.add(MessageNeverSentFix.NAME);
        fixHeuristicsNames.add(MissingCloneInitializationFix.NAME);
        fixHeuristicsNames.add(MissingLoopSensingLoopFix.NAME);
        fixHeuristicsNames.add(MissingLoopSensingWaitFix.NAME);
        fixHeuristicsNames.add(StutteringMovementFix.NAME);
        return fixHeuristicsNames;
    }

    static void registerQuestionFinder(IssueFinder finder, Map<String, IssueFinder> questionFinders) {
        if (finder.getIssueType() != IssueType.QUESTION) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Solution IssueFinder");
        }
        questionFinders.put(finder.getName(), finder);
    }
}

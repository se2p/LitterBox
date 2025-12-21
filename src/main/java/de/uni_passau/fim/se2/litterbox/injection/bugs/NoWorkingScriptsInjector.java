package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;

/**
 * Injects the {@link NoWorkingScripts} bug by splitting a GreenFlag script.
 * <p>
 * The bug pattern requires:
 * <ol>
 *   <li>An event handler (e.g., GreenFlag) with an <b>empty</b> body</li>
 *   <li>Dead code (script with Never event) with a <b>non-empty</b> body</li>
 * </ol>
 * <p>
 * Transformation:
 * <pre>
 * Before: [GreenFlag -> {move 10 steps}]
 * After:  [GreenFlag -> {}], [Never -> {move 10 steps}]
 * </pre>
 * This simulates a scenario where the user accidentally detached code from its event handler.
 */
public class NoWorkingScriptsInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new NoWorkingScripts();
    }

    @Override
    public void visit(ScriptList node) {
        for (int i = 0; i < node.getScriptList().size(); i++) {
            Script script = node.getScript(i);
            if (isGreenFlagWithBody(script)) {
                addSplitScriptMutation(node, i);
            }
        }
        super.visit(node);
    }

    private boolean isGreenFlagWithBody(Script script) {
        return script.getEvent() instanceof GreenFlag
                && !script.getStmtList().getStmts().isEmpty();
    }

    /**
     * Splits a GreenFlag script into an empty event handler and detached dead code.
     *
     * @param scriptList the script list containing the target script
     * @param index      the index of the GreenFlag script to split
     */
    private void addSplitScriptMutation(ScriptList scriptList, int index) {
        addMutation(scriptList, clonedScriptList -> {
            Script originalScript = clonedScriptList.getScript(index);

            // Create empty GreenFlag script (keeps the event, loses the body)
            Script emptyEventHandler = new Script(
                    originalScript.getEvent(),
                    new StmtList(new ArrayList<>())
            );

            // Create detached code with Never event (represents loose blocks without hat)
            Script detachedCode = new Script(
                    new Never(),
                    originalScript.getStmtList()
            );

            // Replace original and insert detached code after it
            clonedScriptList.getScriptList().set(index, emptyEventHandler);
            clonedScriptList.getScriptList().add(index + 1, detachedCode);

            emptyEventHandler.setParentNode(clonedScriptList);
            detachedCode.setParentNode(clonedScriptList);
        });
    }
}

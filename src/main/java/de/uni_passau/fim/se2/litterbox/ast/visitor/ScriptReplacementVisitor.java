package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.ast.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ScriptReplacementVisitor extends OnlyCodeCloneVisitor {

    private final Script target;
    private final Set<Script> otherTargets = Sets.newIdentityHashSet();
    private final List<Script> replacementScripts;

    public ScriptReplacementVisitor(Script target, List<Script> replacement) {
        this.target = target;
        this.replacementScripts = replacement;
    }

    public ScriptReplacementVisitor(Script target, Script... replacement) {
        this.target = target;
        this.replacementScripts = Arrays.asList(replacement);
    }

    public ScriptReplacementVisitor(Script target, List<Script> otherTargets, List<Script> replacement) {
        this.target = target;
        this.otherTargets.addAll(otherTargets);
        this.replacementScripts = replacement;
    }

    protected boolean isTargetScript(ScriptEntity node) {
        return node == target;
    }


    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script script : node.getScriptList()) {
            if (isTargetScript(script)) {
                for (Script replacement : replacementScripts) {
                    scripts.add(replacement);
                }
            } else if (!otherTargets.contains(script)) {
                scripts.add(apply(script));
            }
        }
        return new ScriptList(scripts);
    }
}

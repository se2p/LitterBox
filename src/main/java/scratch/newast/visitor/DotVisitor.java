package scratch.newast.visitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

/**
 * Visitor that creates a .dot output for a Program-AST Currently this is not correct as we use the class name instead
 * of a proper name String representation. This needs to be changed once ASTNodes have a proper toString method.
 */
public class DotVisitor implements ScratchVisitor {

    List<String> edges = new LinkedList<>();

    @Override
    public void visit(ASTNode node) {
        String name = node.getClass().getSimpleName();
        for (ASTNode child : node.getChildren()) {
            String edge = name + " -> " + child.getClass().getSimpleName();
            edges.add(edge);
        }

        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    public void printGraph() {
        System.out.println("digraph G {");
        System.out.println("\t rankdir=LR");
        System.out.println("\t shape=rectangle");
        for (String edge : edges) {
            System.out.print("\t");
            System.out.println(edge);
        }
        System.out.println("}");
    }

    public void saveGraph(String fileName) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        bw.write("digraph G {");
        bw.newLine();
        // bw.write("\t rankdir=LR");
        // bw.newLine();
        bw.write("\t shape=rectangle");
        bw.newLine();
        for (String edge : edges) {
            bw.write("\t");
            bw.write(edge);
            bw.newLine();
        }
        bw.write("}");
        bw.close();
    }
}

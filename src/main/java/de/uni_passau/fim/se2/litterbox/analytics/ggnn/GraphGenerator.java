package de.uni_passau.fim.se2.litterbox.analytics.ggnn;


import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteAndStageVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.GraphVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;

public class GraphGenerator {
    Program program;
    Map<ASTNode, List<ASTNode>> leafsMap;
    private final boolean isStageIncluded;
    private final boolean isWholeProgram;
    private final boolean isDotStringGraph;
    private final String labelName;

    public GraphGenerator(Program program,  boolean isStageIncluded,boolean isWholeProgram,boolean isDotStringGraph,String labelName){
        this.program = program;
        this. isStageIncluded= isStageIncluded;
        this. isWholeProgram= isWholeProgram;
        this. isDotStringGraph= isDotStringGraph;
        this. labelName= labelName;
    }

    public void extractGraphsPerSprite() {    	
    	if(isStageIncluded) {
    		ExtractSpriteAndStageVisitor spriteVisitor = new ExtractSpriteAndStageVisitor();
            program.accept(spriteVisitor);
            leafsMap = spriteVisitor.getLeafsCollector();
    	}
    	else {
    		ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor();
            program.accept(spriteVisitor);
            leafsMap = spriteVisitor.getLeafsCollector();
    	}    	
    }

    public void printLeafsPerSpite() {
        System.out.println("Anzahl der Sprites: " + leafsMap.keySet().size());
        for(ASTNode key : leafsMap.keySet()) {
            System.out.println("Actor Definition: " + ((ActorDefinition)key).getIdent().getName());
            System.out.println("Anzahl an ASTLeafs für " + ((ActorDefinition)key).getIdent().getName() + ": " + leafsMap.get(key).size());
            int i=0;
            for(ASTNode value : leafsMap.get(key)){
                System.out.println(i + "Blatt (Test): " + value.getUniqueName());
                i++;
            }
        }
    }

    public String generateGraphs(String inputPath) {        
        String graphData="";
        
        if (isWholeProgram) {
        	File file = new File(inputPath);
        	String name = (labelName==null||labelName.isEmpty())? file.getName():labelName;
        	graphData=graphData+generateGraphsForWholeScratchProgram(program,name,inputPath);
		}
        else {
        	int countSprite=0;
            String spriteName;
            for(ASTNode sprite : leafsMap.keySet()) { 
            	spriteName = (labelName==null||labelName.isEmpty())? ((ActorDefinition)sprite).getIdent().getName():labelName;
            	graphData=graphData+generateGraphsPerSprite(sprite,spriteName,countSprite,inputPath);      
            	countSprite++;
            }
		}
        return graphData;
    }

    private String generateGraphsPerSprite(ASTNode sprite, String spriteName, int spriteIndex, String inputPath) {
    	
        String incorrectSpriteName=generateRandomString();
        GraphVisitor visitor1 = new GraphVisitor();       
    	sprite.accept(visitor1);
    	ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
    	sprite.accept(visitor);
    	ControlFlowGraph cfg = visitor.getControlFlowGraph();   
    	
    	if (isDotStringGraph) {
    		return visitor1.getBuilderInDotString(cfg);
		}
    	else {
    		return visitor1.printGraph(inputPath, cfg, spriteName, incorrectSpriteName,spriteIndex,leafsMap.keySet().size());
		}	
    }
    
    
    
private String generateGraphsForWholeScratchProgram(Program program , String name, String inputPath) {    	
        String incorrectName=generateRandomString();
        GraphVisitor visitor1 = new GraphVisitor();       
        program.accept(visitor1);
    	ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
    	program.accept(visitor);
    	ControlFlowGraph cfg = visitor.getControlFlowGraph();
    	// int slotIndex=0; 
    	
    	if (isDotStringGraph) {
    		return visitor1.getBuilderInDotString(cfg);
		}
    	else {
        	return visitor1.printGraph(inputPath, cfg, name, incorrectName,0,1);
    	}
    }

    //Generate random string-default length 10 digits
    public static String generateRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
    }
    
}

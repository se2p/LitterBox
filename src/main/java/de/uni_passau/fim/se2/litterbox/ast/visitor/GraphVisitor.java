package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;

import com.google.common.graph.EndpointPair;

public class GraphVisitor implements ScratchVisitor, PenExtensionVisitor  {
	//Variables
	long counter = 0;
	String backwardEdges="";
	Integer countIndexOfVertices=0;
//	String slot="<slot>";
	//Objects
        
    Map<String, List<String>> astEdges = new HashMap<String, List<String>>();
    Map<String, List<String>> siblingEdges = new HashMap<String, List<String>>();
    Map<String, List<String>> nextTokenEdges = new HashMap<String, List<String>>();
    Map<String, List<String>> nextUseEdges = new HashMap<String, List<String>>();
    
    
    List<String> terminalNodes = new ArrayList<>();    
    List<String> nodeLabels =  new ArrayList<>();
    List<String> terminalVertices = new ArrayList<>();
    
    Map<String,String> vertexMap = new HashMap<String,String>();
    Map<String,String> vertexMapForFlowEdges = new HashMap<String,String>();
    Map<String,String> vertexMapForDotString = new HashMap<String,String>();
    
//    List<Triplet<String,String,String>> verticesList= new ArrayList<Triplet<String,String,String>>(List.of(Triplet.with("0", "0", "<slot>")));
    List<Triplet<String,String,String>> verticesList= new ArrayList<Triplet<String,String,String>>();
    
    List<String> vertices = new ArrayList<>();
    
    @Override
    public void visit(ASTNode node) {
    	List<String> vertexList = new ArrayList<>();
        if (node instanceof ASTLeaf) {
            recordLeaf((ASTLeaf) node);
        } else {        	
            String name = String.valueOf(node.hashCode()); //This should only be a workaround this is a hack  
            verticesList.add(Triplet.with(String.valueOf(countIndexOfVertices), name, node.getUniqueName()));            
            countIndexOfVertices++;                        
            // astEdges.computeIfAbsent(slot, k -> new ArrayList<>()).add(String.valueOf(name));
            
            for (ASTNode child : node.getChildren()) {       
            	// astEdges.computeIfAbsent(slot, k -> new ArrayList<>()).add(String.valueOf(child.hashCode()));
                astEdges.computeIfAbsent(name, k -> new ArrayList<>()).add(String.valueOf(child.hashCode()));                
                vertexList.add(String.valueOf(child.hashCode()));  
            }      
            
            addSiblingEdges(vertexList,0);
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    public void recordLeaf(ASTLeaf node) {
    	List<String> vertexList = new ArrayList<>();
        String name = String.valueOf(node.hashCode());

        verticesList.add(Triplet.with(String.valueOf(countIndexOfVertices), name, node.getUniqueName()));            
        countIndexOfVertices++;        
        
        // astEdges.computeIfAbsent(slot, k -> new ArrayList<>()).add(String.valueOf(name));
        String[] simpleStrings = node.toSimpleStringArray();
        String uniqueId="";
        
        if (simpleStrings.length==0) {
        	terminalVertices.add(String.valueOf(name));
		}
        
        for (String simpleString : simpleStrings) {
            counter++;
            uniqueId=UUID.randomUUID().toString().replace("-","").replaceAll("\\D+","");            
            astEdges.computeIfAbsent(name, k -> new ArrayList<>()).add(String.valueOf(uniqueId));            
            // astEdges.computeIfAbsent(slot, k -> new ArrayList<>()).add(String.valueOf(uniqueId));            
            vertexList.add(String.valueOf(uniqueId));
            terminalVertices.add(String.valueOf(uniqueId));
            
            verticesList.add(Triplet.with(String.valueOf(countIndexOfVertices), uniqueId, simpleString));            
            countIndexOfVertices++;           
        }
        addSiblingEdges(vertexList,0);
    }
  
    public void initialize() {  	
    	backwardEdges="";
    	// Index, hash code and name
    	for (Triplet<String, String, String> triplet : verticesList) {
    		vertices.add(triplet.getValue1());//hash code
    		nodeLabels.add(triplet.getValue2());//name
    		vertexMap.put(triplet.getValue1(),triplet.getValue0());//hash code and index
    		vertexMapForFlowEdges.put(triplet.getValue2(),triplet.getValue1());//name and hash code
    		vertexMapForDotString.put(triplet.getValue1(),triplet.getValue2());//hash code and name
		}
    }
    
    public void toDotString(ControlFlowGraph cfg) {    	
    	backwardEdges="";      
        System.out.println(getBuilderInDotString(cfg));
        System.out.println("}");
    }
    
    //StringBuilder in dotString
    public String getBuilderInDotString(ControlFlowGraph cfg) {
    	StringBuilder builder = new StringBuilder();
    	List<String> terminalVertexList = new ArrayList<>(new HashSet<>(terminalVertices));
    	initialize();
    	builder.append("digraph G {");
    	builder.append("\n");
    	builder.append("\t shape=rectangle");
        builder.append("\n");  
        builder.append("\t");
        builder.append(getEdgesForGNN(astEdges,0,true, false));
        builder.append("\n");
        builder.append(getEdgesForGNN(astEdges,1,true, false));
        builder.append("\t");
        builder.append(getEdgesForGNN(siblingEdges,0,true, false));
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(checkAndAddNextTokenEdges(terminalVertexList),0,true, false));
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(getFlowEdges(getNextUseEdges(cfg)),0,true, false));
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(getFlowEdges(cfg.getFlowEdges()),0,true, true));
        builder.append("\n");
        builder.append("\t");
        builder.append(backwardEdges);
        builder.append("\n");
        builder.append("\t");
        builder.append("}");
    	
    	return builder.toString();
    }
    
    
    
    //Print Graph in console
    public String printGraph(String fileName,ControlFlowGraph controlFlowGraph
    		,String correctLabel,String incorrectLabel,int spriteIndex, int lastIndex) {    	 	
    	return createBuilderForGNN( fileName, controlFlowGraph, correctLabel, incorrectLabel, spriteIndex,  lastIndex);
    }
    
//    //Save Graph
//    public void saveGraph(String fileName,ControlFlowGraph controlFlowGraph
//    		,String correctLabel,String incorrectLabel,String outFolderName,int spriteIndex, int lastIndex, string format) throws IOException {    
//    	Path pathToFolder = Path.of(outFolderName);
//    	Path pathToFile = pathToFolder.resolve(correctLabel);
//    	File file = new File(correctLabel);
//        FileOutputStream fos = new FileOutputStream(file);
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
//        bw.write(createBuilderForGNN( fileName, controlFlowGraph,correctLabel,incorrectLabel,spriteIndex,lastIndex));
//        bw.close();
//    } 
    
    //String Builder for generating embeddings in string
    private String createBuilderForGNN(String fileName,ControlFlowGraph controlFlowGraph,
    		String correctLabel,String incorrectLabel,int spriteIndex, int lastIndex) {
    	List<String> terminalVertexList = new ArrayList<>(new HashSet<>(terminalVertices));  
    	initialize();
    	StringBuilder builder = new StringBuilder();
    	backwardEdges="";
//    	if (spriteIndex==0) {
//    		builder.append("[");
//		}
        builder.append("{\"filename\": \""+fileName+"\", ");
        builder.append("\"ContextGraph\": {");
        builder.append("\"Edges\": {");
        builder.append("\"Child\": [");         
        builder.append(getEdgesForGNN(astEdges,0,false, false));    	
        builder.append("], ");
        builder.append("\"Parent\": [");   
        builder.append(getEdgesForGNN(astEdges,1,false, false));    	
        builder.append("], ");
        builder.append("\"NextSib\": [");   
        builder.append(getEdgesForGNN(siblingEdges,0,false, false));    	
        builder.append("], ");   
        builder.append("\"NextToken\": [");   
        builder.append(getEdgesForGNN(checkAndAddNextTokenEdges(terminalVertexList),0,false,false));    	
        builder.append("], ");
        builder.append("\"NextUse\": [");     	
        builder.append(getEdgesForGNN(getFlowEdges(getNextUseEdges(controlFlowGraph)),0,false, false));    	
        builder.append("], ");  
        builder.append("\"Flow\": [");   
        builder.append(getEdgesForGNN(getFlowEdges(controlFlowGraph.getFlowEdges()),0,false, true));  
        builder.append("], ");  
        builder.append("\"Backward\": [");   
        builder.append(backwardEdges);    	
        builder.append("] ");  
        builder.append("}},");
        builder.append("\"NodeLabels\": {");
        builder.append(getNodes(nodeLabels));  
        builder.append("},");
        builder.append("\"NodeTypes\": {");
        List<String> nodeTypes =new LinkedList<>(nodeLabels);
        nodeTypes.remove(0);
        Set<String> nodeTypesWithoutDuplicates = new LinkedHashSet<>(nodeTypes);
        nodeTypes.clear();
        nodeTypes.addAll(nodeTypesWithoutDuplicates);
        builder.append(getNodes(nodeTypes));  
        builder.append("},");
//        builder.append("\"SlotDummyNode\":0,");
        builder.append("\"SymbolCandidates\":[");    
        builder.append(getSymbolCandidates( correctLabel, incorrectLabel));      
        builder.append("]}");
        if (spriteIndex!=lastIndex-1) {
        	builder.append("\n");      	
		}
//        else {
//        	builder.append("]");
//		}
        
        return builder.toString();
    	
    }
    
    //Return symbol candidates
    private String getSymbolCandidates(String correctlabel, String incorrectlabel) {
    	String symbolCandidatesInString="{\"SymbolDummyNode\":1, \"SymbolName\":\""+correctlabel+ "\",\"IsCorrect\":true},";
    	symbolCandidatesInString=symbolCandidatesInString+"{\"SymbolDummyNode\":2, \"SymbolName\":\""+incorrectlabel+ "\",\"IsCorrect\":false}";
    	return symbolCandidatesInString;
    }
    
    //Get Flow edges
    private Map<String, List<String>> getFlowEdges(Map<String, List<String>> flowEdges) {
    	List<String>tempList =new ArrayList<>();
    	Map<String, List<String>> flowEdgesInHashCode = new HashMap<String, List<String>>();
    	Set<String> tempListWithoutDuplicates;

    	String firstVertex="";
    	String secondVertex="";
    	for (Map.Entry<String, List<String>> entry : flowEdges.entrySet()) {
    		if(vertexMapForFlowEdges.containsKey((entry.getKey()))) {
    			firstVertex=String.valueOf(vertexMapForFlowEdges.get(entry.getKey()));
    		}
    		for (String targetVertex : entry.getValue()) {    
    			if(vertexMapForFlowEdges.containsKey(targetVertex)) {
    				secondVertex=String.valueOf(vertexMapForFlowEdges.get(targetVertex));
        		}
    			if(StringUtils.isBlank(firstVertex)==false && StringUtils.isBlank(secondVertex)==false) {
    				tempList.add(secondVertex);
    			}
    		}
    		tempListWithoutDuplicates=new LinkedHashSet<>(tempList);
    		if(StringUtils.isBlank(firstVertex)==false) {
    			for (String string : tempListWithoutDuplicates) {
    				flowEdgesInHashCode.computeIfAbsent(firstVertex, k -> new ArrayList<>()).add(String.valueOf(secondVertex));
				}
    		}
    		
    	}
    	return flowEdgesInHashCode;
    }
    
    //Return nodes in String
    private String getNodes(List<String> nodeTypes) {    	
    	String nodesInString="";
    	int indexOfNodes=0;
    	for (String node : nodeTypes) {
    		nodesInString=nodesInString+"\""+String.valueOf(indexOfNodes)+"\":";
    		nodesInString=nodesInString+"\""+String.valueOf(node)+"\",";
    		indexOfNodes++;
		}
    	return (nodesInString.length()>0)?nodesInString.substring(0, nodesInString.length() - 1):nodesInString;
    }
    
    //Return Next Token edges
    private Map<String, List<String>> checkAndAddNextTokenEdges(List<String> terminalVertices) {  
    	if(terminalVertices.size()>1) {
    		for (int i = 0; i < terminalVertices.size()-1; i++) {
    			nextTokenEdges.computeIfAbsent(terminalVertices.get(i), k -> new ArrayList<>()).add(String.valueOf(terminalVertices.get(i+1))); 
    		}
    	}
    	return nextTokenEdges;     	
    }
    
    //Return NextUse Edges - Exploit data flow information in AST
    private Map<String, List<String>> getNextUseEdges(ControlFlowGraph controlFlowGraph) {
    	DataDependenceGraph  ddg=new DataDependenceGraph(controlFlowGraph);    	
    	for (EndpointPair<CFGNode> edge : ddg.getEdges()) {  
    		String[] nodeUTexts = edge.nodeU().toString().split("\\.");
    		String nodeU=nodeUTexts[nodeUTexts.length-1].split("\\@")[0];
    		String[] nodeVTexts = edge.nodeV().toString().split("\\.");
    		String nodeV=nodeVTexts[nodeVTexts.length-1].split("\\@")[0];    	
    		nextUseEdges.computeIfAbsent(nodeU, k -> new ArrayList<>()).add(nodeV);
		}
    	return nextUseEdges;    	
    }
    
    //Checks and add sibling edges
    private void addSiblingEdges(List<String> vertexList,int edgeType) {
    	if(vertexList.size()>1) {    		
        	for (int i = 0; i < vertexList.size()-1; i++) 
        	{      
        		siblingEdges.computeIfAbsent(vertexList.get(i), k -> new ArrayList<>()).add(String.valueOf(vertexList.get(i+1))); 
        	}            	
        }
    }
    
    //Get edges for Graph in string
    private String getEdgesForGNN(Map<String, List<String>> edges, int typeOfEdge, Boolean isDotString, Boolean isSlotIncluded) {
    	String firstVertex="0";
    	String secondVertex;
    	String edgesInString="";
    	String operatorSymbol=" -> ";
//    	String slotVertex="";
    	String backwardEdgesInString="";
    	Map<String,String> vertexList;
    	if(!isDotString) {
    		operatorSymbol=",";
    		vertexList=vertexMap;
    	}
    	else {
    		vertexList=vertexMapForDotString;
    	}
    	
    	Integer numberOfedges=edges.size();
    	Integer edgeIndex=0;
    	List<String> edgeValues ;
    	for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
    		firstVertex="";
    		edgeIndex++;
    		if(vertexList.containsKey(entry.getKey())) {
    			firstVertex=(!isDotString)?String.valueOf(vertexList.get(entry.getKey())):entry.getKey();
    			if(isDotString) {
    				edgesInString=edgesInString+firstVertex +" [label= \""+String.valueOf(vertexList.get(entry.getKey())+"\"];");
        			edgesInString=edgesInString+"\n";
    			}
    			
    		}    		
//    		if(entry.getKey()==slot) {
//    			firstVertex="0";
//    			if(isDotString) {
//    				edgesInString=edgesInString+firstVertex +" [label= \""+String.valueOf(entry.getKey()+"\"];");
//        			edgesInString=edgesInString+"\n";
//    			}	
//    		}    		
//    		if(isSlotIncluded) {
//    			if(!isDotString) {
//        			edgesInString=edgesInString+"[";  
//        			backwardEdgesInString=backwardEdgesInString+"[";
//        		}    			
//    			slotVertex=String.valueOf(vertexList.get("0"));
//    			edgesInString=edgesInString+ slotVertex +operatorSymbol+firstVertex;
//        		backwardEdgesInString=backwardEdgesInString+ firstVertex +operatorSymbol+slotVertex;
//        		
//        		if(!isDotString) {
//        			edgesInString=edgesInString+"]";  
//        			backwardEdgesInString=backwardEdgesInString+"]";
//        		}
//        		else {
//        			edgesInString=edgesInString+"\n";
//        			backwardEdgesInString=backwardEdgesInString+"\n";
//        		}
//    		}
    		edgeValues=(!isDotString)?entry.getValue():new ArrayList<>( new HashSet<>(entry.getValue()));	
    		for (String targetVertex : edgeValues) {    			
        		secondVertex="";
        		
        		if(vertexList.containsKey(targetVertex)) {
        			secondVertex=vertexList.get(targetVertex);
        			if(isDotString) {
        				secondVertex=targetVertex;
        				edgesInString=edgesInString+secondVertex +" [label= \""+String.valueOf(vertexList.get(secondVertex)+"\"];");
            			edgesInString=edgesInString+"\n";
        			}	
        		}
//        		if(isSlotIncluded) {
//        			if(!isDotString) {
//            			edgesInString=edgesInString+"[";
//            			backwardEdgesInString=backwardEdgesInString+"[";
//            		}
//        			edgesInString=edgesInString+ slotVertex +operatorSymbol+secondVertex;
//        			backwardEdgesInString=backwardEdgesInString+ secondVertex +operatorSymbol+slotVertex;
//        			if(!isDotString) {
//            			edgesInString=edgesInString+"]"+operatorSymbol;
//            			backwardEdgesInString=backwardEdgesInString+"]"+operatorSymbol;
//            		}
//        			else {
//        				edgesInString=edgesInString+"\n";
//            			backwardEdgesInString=backwardEdgesInString+"\n";
//        			}
//        		}
        		if(!isDotString) {
        			edgesInString=edgesInString+"[";        			
        			backwardEdgesInString= backwardEdgesInString+"[";
        		}
        		if (typeOfEdge==0) {
        			//Child edge
        			edgesInString=edgesInString+ firstVertex +operatorSymbol+secondVertex;
        			backwardEdgesInString=backwardEdgesInString+ secondVertex +operatorSymbol+firstVertex;
        			
        		}
        		else if(typeOfEdge==1) {
        			//Parent edge
        			edgesInString=edgesInString+ secondVertex +operatorSymbol+firstVertex;
        			backwardEdgesInString=backwardEdgesInString+ firstVertex +operatorSymbol+secondVertex;        			
        		}
        		      		
        		if(!isDotString) {
        			edgesInString=edgesInString+"]";
        			backwardEdgesInString=backwardEdgesInString+"]";
        			if (edgeIndex<=numberOfedges) {
        				edgesInString=edgesInString+operatorSymbol;
        				backwardEdgesInString=backwardEdgesInString+operatorSymbol;
					}
        		}
        		else {
        			edgesInString=edgesInString+"\n";
        			backwardEdgesInString=backwardEdgesInString+"\n";
        		}
			}
    	}
    	backwardEdges=(backwardEdges.isEmpty())?backwardEdges+backwardEdgesInString:backwardEdges+','+backwardEdgesInString;
    	if (!isDotString) {
    		edgesInString=(edgesInString.length()>0)?edgesInString.substring(0, edgesInString.length() - 1):edgesInString;  
        	backwardEdges=(backwardEdges.length()>0)?backwardEdges.substring(0, backwardEdges.length() - 1):backwardEdges; 
		}
    	
    	return edgesInString;
    }
    

    @Override
    public void visit(PenStmt node) {
        node.accept((PenExtensionVisitor) this);
    }

    @Override
    public void visitParentVisitor(PenStmt node){
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(PenDownStmt node) {
        if (node != null) {
            recordLeaf(node);
        }
    }

    @Override
    public void visit(PenUpStmt node) {
        if (node != null) {
            recordLeaf(node);
        }
    }
}

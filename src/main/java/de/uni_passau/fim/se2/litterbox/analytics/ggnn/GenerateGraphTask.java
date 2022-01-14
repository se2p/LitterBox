package de.uni_passau.fim.se2.litterbox.analytics.ggnn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class GenerateGraphTask {

	private final Program program;	
	private final boolean isStageIncluded;
	private final boolean isWholeProgram;
	private final String output;
	private final boolean isDotStringGraph;
	private final String labelName;
	
	public GenerateGraphTask(Program program, String inputPath,boolean isStageIncluded,boolean isWholeProgram, String output, boolean isDotStringGraph,String labelName){
        this.program = program;
        this.isStageIncluded = isStageIncluded;
        this.isWholeProgram = isWholeProgram;
        this.output = output;
        this.isDotStringGraph=isDotStringGraph;
        this.labelName=labelName;
        createGraphForGNN(inputPath);
    }
	
	private void createGraphForGNN(String inputPath) {
		//To Do
		GraphGenerator graphGenerator = new GraphGenerator(program,isStageIncluded,isWholeProgram,isDotStringGraph,labelName);
		if (!isWholeProgram) {
			graphGenerator.extractGraphsPerSprite();
			//graphGenerator.printGraphsPerSpite();
		}
		String toPrint = graphGenerator.generateGraphs(inputPath);
		if (output==null||StringUtils.isEmpty(output)) {			
	        if (toPrint.length() > 0) {
	            System.out.println(toPrint);
	        }
		}
		else {
			try {
				File directory = new File(output);
				if (directory.exists()) {				
					String format = (isDotStringGraph)? ".dot":".txt";
					Path pathToFolder = Path.of(output);
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					Path pathToFile = pathToFolder.resolve("GraphData"+timeStamp+format);
					FileOutputStream fos;
					fos = new FileOutputStream(pathToFile.toString());
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
					bw.write(toPrint);
			        bw.close();
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
        
    }
}

# LitterBox

Static code analysis tool for detecting recurring bug patterns in Scratch projects. 

## Contributors 

This project is developed at the Chair of Software Engineering II in Passau, Germany.

List of contributors:

Christoph Frädrich
Gordon Fraser
Ute Heuer
Nina Körber
Stephan Lukasczyk
Florian Obermüller
Andreas Stahlbauer
Florian Sulzmeier

LitterBox was created in the project FR 2955/3-1 funded by the "Deutsche Forschungsgemeinschaft".

## Usage

LitterBox can analyze a single project and produce console output for every single Issue.

Also, there is a method that takes a folder path as an input and analyzes all Json files in the directory. 
This method produces a csv file with all issue counts for every project.

Finally it is possible to download one or multiple projects and analyze those.

##
To use LitterBox with the command line, build the Jar with mvn clean and mvn package.

### Command Line Options:

1. path - the Scratch projects path or a folder path with multiple Scratch projects
2. projectid - id of the project that should be downloaded and analysed
3. projectlist - path to a file with a list of project ids of projects which should be downloaded and analysed.
4. projectout - path where downloaded projects should be stored
5. output - path to the csv file where the results of the analysis should be stored
6. detectors - all the detectors you want to run (short names separated by ","), if not set, all will be used

#### Detectors short names:


Detectors:
  
	all                  All issue finders               
	bugs                 All issue finders for bug patterns      
	smells               All issue finders for smells    
	ctscore              All issue finders for ct scores
	
	Bugpatterns:
	ambCustBlSign        Ambiguous Custom Block Signature 
	ambParamName         Ambiguous Parameter Name
	ambParamNameStrct    Ambiguous Parameter Name Strict
	cllWithoutDef        Call Without Definition
	compLit              Comparing Literals
	custBlWithForever    Custom Block With Forever
	custBlWithTerm       Custom Block With Termination  
	endlRec              Endless Recursion
	exprTouchColor       Expression As Touching Or Color
	foreverInLoop        Forever inside Loop
	illParamRefac        Illegal Parameter Refactor
	messNeverSent        Message Never Sent
	messNeverRec         Message Never Received  
	mssBackdrSwitch      Missing Backdrop Switch
	mssCloneCll          Missing Clone Call
	mssCloneInit         Missing Clone Initialization
	mssEraseAll          Missing Erase All
	mssLoopSens          Missing Loop Sensing
	mssPenDown           Missing Pen Down	
	mssPenUp             Missing Pen Up
	mssTerm              Missing Termination 
	mssWaitCond          Missing Wait Until Condition
	noWorkScript         No Working Script
	orphParam            Orphaned Parameter
	paramOutScope        Parameter out of Scope
	posEqCheck           PositionEqualsCheck 
	recClone             Recursive Cloning
	sameVarDiffSprite    Same Variable used in Different Sprite    
	stuttMove            Stuttering Movement	
	
	Smells:
	empCtrlBody          Empty Control Body  
	empCustBl            Empty Custom Block
	empProj              Empty Project	
	empScript            Empty Script
	empSprite            Empty Sprite
	dcode                Dead Code	
	longScript           Long Script 
	nestLoop             Nested Loops
	unusedCustBl         Unused Custom Block               
	unusedVar            Unused Variable 
                  
	CT-Score:  
	flow                 Flow Control                
	                          
	Utils:   
	blockCnt             Block Count
	procCnt              Procedure Count
	spriteCnt            Sprite Count 
	usingPen             Using Pen
	weightedMethCnt      Weighted Method Count           


#### Example:

java -cp C:\ScratchAnalytics-1.0.jar de.uni_passau.fim.se2.litterbox.Main -path C:\scratchprojects\files\ -output C:\scratchprojects\files\test.csv -detectors mssLoopSens,blockCnt

This will run only Missing Loop Sensing and Block Count on all projects in C:\scratchprojects\files\; and it will also
 save the test.csv file in the same location.

## Extendability

First of all, create a new IssueFinder and implement the corresponding interface. 
The check() method must return a IssueReport with the issue name, count for the current project, 
the sprites of all issue occurrences and notes about the issue.
Then, register the newly created IssueFinder in the IssueTool constructor ( finder.add(new NewFinder()) ).
The finder name will automatically be added to the printed csv file.


## Publications
C. Frädrich, F. Obermüller, N. Körber, U. Heuer, and G. Fraser, “Common bugs in scratch programs,” in Proceedings of
 the 25th Annual Conference on Innovation and Technology in Computer Science Education (ITiCSE), 2020, to appear.

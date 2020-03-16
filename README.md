# LitterBox

[![pipeline status](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/badges/master/pipeline.svg)](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/pipelines)
[![coverage report](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/badges/master/coverage.svg)](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/commits/master)


Static code analysis tool for detecting recurring bug patterns in Scratch projects. 

## Authors

See [Contributors](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/-/graphs/master)

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
5. detectors - all the detectors you want to run (short names seperated by ","), if not set, all will be used

#### Detectors short names:


Detectors:
  
	all                  All issue finders               
	bugs                 All issue finders for bug patterns      
	smells               All issue finders for smells    
	ctscore              All issue finders for ct scores
	
	Bugpatterns:
	ambCustBlSign        Ambiguous Custom Block Signature 
	ambParamName         Ambiguous Parameter Name
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
	usingPen             Using Pen
                  
	CT-Score:  
	flow                 Flow Control                
	                          
	Utils:   
	blockCnt             Block Count
	procCnt              Procedure Count
	spriteCnt            Sprite Count 
	weightedMethCnt      Weighted Method Count           


#### Example:

java -cp C:\ScratchAnalytics-1.0.jar de.uni_passau.fim.se2.litterbox.Main -path C:\scratchprojects\files\ -version 3 -folder C:\scratchprojects\files\test.csv -detectors cnt,glblstrt

This will run only BlockCount and GlobalStartingpoint on all projects in C:\scratchprojects\files\; and it will also save the test.csv file in the same location.

## Extendability

First of all, create a new IssueFinder and implement the corresponding interface. 
The check() method must return a IssueReport with the issue name, count for the current project, 
the position of all issue occurrences, the project path and notes about the issue.
Then, register the newly created IssueFinder in the IssueTool constructor ( finder.add(new NewFinder()) ).
The finder name will automatically be added to the printed csv file.

## Publications

[1] Florian Sulzmeier, “Identification and Automated Analysis of Common Bug Pattern in Scratch Programs,” Bachelor Thesis, Passau, Passau, 2019. - [Olf96](https://github.com/Olf96)

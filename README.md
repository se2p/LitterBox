# LitterBox

Static code analysis tool for detecting recurring bug patterns in Scratch projects. 
Currently holding 25 different detectors.

## Author

* **Florian Sulzmaier** - [Olf96](https://github.com/Olf96)

## Usage

LitterBox can analyze a single project (checkSingle()) and produce console output 
for every single Issue.

Also, there is a method (checkMultiple()) that takes a folder path as an input and analyzes all zip files within the directory. 
This method produces a csv file with all issue counts for every project.

## Extendability

First of all, create a new IssueFinder and implement the corresponding interface. 
The check() method must return a IssueReport with the issue name, count for the current project, 
the position of all issue occurrences, the project path and notes about the issue.
Then, register the newly created IssueFinder in the IssueTool constructor ( finder.add(new NewFinder()) ).
The finder name will automatically be added to the printed csv file.
package analytics.clone.detection;

import java.util.ArrayList;
import java.util.List;


/**
 * This class normalize the script of the project.
 */
public class Normalization {

	/**
	 * This method normalize the script. Customized blocks will be 
	 * changed to DUMMY or if is it a number to NUMBER.
	 * @param project The project whose script should be normalize.
	 * @return The normalized script.
	 */
	public List<String> codeNormalization(List<String> preparatedCode) {
		List<String> normalizedDropDown = normalizationDropDown(preparatedCode);
		List<String> normalizedNumber = normalizationNumbers(normalizedDropDown);
		List<String> normalizedSensing = normalizationSensing(normalizedNumber);
		List<String> normalizedOperator = normalizationOperator(normalizedSensing);
		List<String> normalizedVariable = normalizationVariable(normalizedOperator);
		return normalizedVariable;
	}
	
	/*
	 * If in a block appears , then " then a text and then ", then the text will 
	 * be changed to VARIABLE.
	 */
	private List<String> normalizationVariable(List<String> list) {
		List<String> normalizedScript = new ArrayList<String>(list.size());
		for(int k = 0; k < list.size(); k++) {
			char[] ch = list.get(k).toCharArray();
			List<Character> listBlock = new ArrayList<Character>();
			for(char c : ch) {
				listBlock.add(c);
			}
			for(int i = 0; i < listBlock.size(); i++) {
				if(i + 2 < listBlock.size() && listBlock.get(i + 1) == ',' && 
					listBlock.get(i + 2) == '"') {
					int j = i + 3;
					boolean notLastQuote = false;
					for(int b = j; b < listBlock.size(); b++) {
						if(listBlock.get(b) == '"') {
							notLastQuote = true;
							break;
						}
					}
					while(notLastQuote && listBlock.get(j) != '"') {
						listBlock.remove(j);
					}
					listBlock = writeVariable(listBlock, j);
				}
			}
			StringBuilder sb = new StringBuilder();
			for(Character c : listBlock) {
				sb.append(c);
			}
			normalizedScript.add(sb.toString());
		}
		return normalizedScript;
	}
	
	/*
	 * If in a block appears "" then a text and then ", then the text will 
	 * be changed to DUMMY.
	 */
	private List<String> normalizationDropDown(List<String> list) {
		List<String> normalizedScript = new ArrayList<String>(list.size());
		for(int k = 0; k < list.size(); k++) {
			char[] ch = list.get(k).toCharArray();
			List<Character> listBlock = new ArrayList<Character>();
			for(char c : ch) {
				listBlock.add(c);
			}
			for(int i = 0; i < listBlock.size(); i++) {
				if(i + 2 < listBlock.size() && listBlock.get(i + 1) == '"' && 
					listBlock.get(i + 2) == '"') {
					int j = i + 3;
					boolean notLastQuote = false;
					for(int b = j; b < listBlock.size(); b++) {
						if(listBlock.get(b) == '"') {
							notLastQuote = true;
							break;
						}
					}
					while(notLastQuote && listBlock.get(j) != '"') {
						listBlock.remove(j);
					}
					listBlock = writeDummy(listBlock, j);
				}
			}
			StringBuilder sb = new StringBuilder();
			for(Character c : listBlock) {
				sb.append(c);
			}
			normalizedScript.add(sb.toString());
		}
		return normalizedScript;
	}
	
	//If a number exists in the script, then this will be changed to NUMBER.
	private List<String> normalizationNumbers(List<String> list) {
		List<String> normalizedScript = new ArrayList<String>(list.size());
		for(int k = 0; k < list.size(); k++) {
			char[] ch = list.get(k).toCharArray();
			List<Character> listBlock = new ArrayList<Character>();
			for(char c : ch) {
				listBlock.add(c);
			}
			int firstNumber = -1;
			for(int i = 0; i < listBlock.size(); i++) {
				if((Character.isDigit(listBlock.get(i)) || (listBlock.get(i) 
					== '-' && Character.isDigit(listBlock.get(i + 1)))) 
					&& !list.get(k).contains("~end")) {
					if(firstNumber == -1) {
					    firstNumber = i;
					}
					listBlock.remove(i);
					i--;
					if(i + 1 < listBlock.size() && listBlock.get(i + 1) == '.') {
						listBlock.remove(i + 1);
					}
					if(i + 1 < listBlock.size() && !Character.isDigit(listBlock.get(i+1)) 
					    && listBlock.get(i+1) != '.') {
						listBlock = writeNumber(listBlock, firstNumber);
						firstNumber = -1;
					}
				}
			}
			listBlock = writeNumber(listBlock, firstNumber);
			StringBuilder sb = new StringBuilder();
			for(Character c : listBlock) {
				sb.append(c);
			}
			normalizedScript.add(sb.toString());
		}
		return normalizedScript;
	}
	
	/*
	 * If in a block appears :"," then a text and then ", then the text will 
	 * be changed to DUMMY.
	 */
	private List<String> normalizationSensing(List<String> list) {
		List<String> normalizedScript = new ArrayList<String>(list.size());
		for(int k = 0; k < list.size(); k++) {
			char[] ch = list.get(k).toCharArray();
			List<Character> listBlock = new ArrayList<Character>();
			for(char c : ch) {
				listBlock.add(c);
			}
			for(int i = 0; i < listBlock.size(); i++) {
				if(i + 3 < listBlock.size() && listBlock.get(i) == ':' && 
					listBlock.get(i + 1) == '"' && listBlock.get(i + 2) 
					== ',' && listBlock.get(i + 3) == '"') {
					int j = i + 4;
					while(listBlock.get(j) != '"') {
						listBlock.remove(j);
					}
					listBlock = writeDummy(listBlock, j);
				}
			}
			StringBuilder sb = new StringBuilder();
			for(Character c : listBlock) {
				sb.append(c);
			}
			normalizedScript.add(sb.toString());
		}
		return normalizedScript;
	}
	
	// If a operator is in a block, it will be changed to OPERATOR.
	private List<String> normalizationOperator(List<String> list) {
		List<String> normalizedScript = new ArrayList<String>(list.size());
		for(int k = 0; k < list.size(); k++) {
			char[] ch = list.get(k).toCharArray();
			List<Character> listBlock = new ArrayList<Character>();
			for(char c : ch) {
				listBlock.add(c);
			}
			for(int i = 0; i < listBlock.size(); i++) {
				
				// Normalization Operators.
				if(listBlock.get(i) == '+' || listBlock.get(i) == '-' || 
						listBlock.get(i) == '*'|| listBlock.get(i) == '/' 
						|| listBlock.get(i) == '<'|| listBlock.get(i) == '>' 
						|| listBlock.get(i) == '=' || listBlock.get(i) == '&'
						|| listBlock.get(i) == '|' || listBlock.get(i) == '%') {
					listBlock.remove(i);
					listBlock = writeOperator(listBlock, i);
				}
				
				// Normalization "not".
				if(i + 2 < listBlock.size() && listBlock.get(i) == 'n'
						&& listBlock.get(i + 1) == 'o' && listBlock.get(i + 2) == 't') {
					for(int j = 0; j < 3; j++) {
					    listBlock.remove(i);
					}
					listBlock = writeOperator(listBlock, i);
				}
				
				// Normalization "rounded".
				if(i + 6 < listBlock.size() && listBlock.get(i) == 'r'
						&& listBlock.get(i + 1) == 'o' && listBlock.get(i + 2) == 'u'
						&& listBlock.get(i + 3) == 'n' && listBlock.get(i + 4) == 'd'
						&& listBlock.get(i + 5) == 'e' && listBlock.get(i + 6) == 'd') {
					for(int j = 0; j < 7; j++) {
					    listBlock.remove(i);
					}
					listBlock = writeOperator(listBlock, i);
				}
				
			}
			StringBuilder sb = new StringBuilder();
			for(Character c : listBlock) {
				sb.append(c);
			}
			normalizedScript.add(sb.toString());
		}
		return normalizedScript;
	} 
	
	// Writes at a specific index NUMBER in a Character list. 
	private List<Character> writeNumber(List<Character> list, int index) {
		if(index > -1) {
		    list.add(index, 'N');
		    list.add(index + 1, 'U');
		    list.add(index + 2, 'M');
		    list.add(index + 3, 'B');
		    list.add(index + 4, 'E');
		    list.add(index + 5, 'R');
		}
		return list;
	}
	
	// Writes at a specific index DUMMY in a Character list. 
	private List<Character> writeDummy(List<Character> list, int index) {
		if(index > -1) {
		    list.add(index, 'D');
		    list.add(index + 1, 'U');
		    list.add(index + 2, 'M');
		    list.add(index + 3, 'M');
		    list.add(index + 4, 'Y');
		}
		return list;
	}
	
	// Writes at a specific index OPERATOR in a Character list. 
	private List<Character> writeOperator(List<Character> list, int index) {
		if(index > -1) {
		    list.add(index, 'O');
		    list.add(index + 1, 'P');
		    list.add(index + 2, 'E');
		    list.add(index + 3, 'R');
		    list.add(index + 4, 'A');
		    list.add(index + 5, 'T');
		    list.add(index + 6, 'O');
		    list.add(index + 7, 'R');
		}
		return list;
	}
	
	private List<Character> writeVariable(List<Character> list, int index) {
		if(index > -1) {
		    list.add(index, 'V');
		    list.add(index + 1, 'A');
		    list.add(index + 2, 'R');
		    list.add(index + 3, 'I');
		    list.add(index + 4, 'A');
		    list.add(index + 5, 'B');
		    list.add(index + 6, 'L');
		    list.add(index + 7, 'E');
		}
		return list;
	}
}

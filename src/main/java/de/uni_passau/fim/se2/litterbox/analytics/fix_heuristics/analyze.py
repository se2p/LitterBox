import os
import re

def analyze_fixes(directory):
    issues = []
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith("Fix.java") and file != "AbstractFixHeuristic.java":
                path = os.path.join(root, file)
                with open(path, 'r') as f:
                    content = f.read()
                
                # Check for visit methods
                # Simple regex to find public void visit(Type name)
                # Then check body for super.visit or visitChildren
                
                visit_pattern = re.compile(r'public void visit\s*\(([^)]+)\)\s*\{')
                
                # Split content into lines for easier processing or matching
                # But matching braces is hard with regex. 
                # I'll just do a rough check: if "visit(" matches, does the whole file contain "visitChildren" or "super.visit"?
                # This is weak. Better to assume standard formatting or just check for existence.
                
                # Actually, if a class overrides visit, it usually handles traversal.
                # If it doesn't call super.visit or visitChildren, traversal stops.
                
                # Let's simple check: pattern find all visit methods. 
                # For each match, we need to verify the body contains super.visit(param) or visitChildren(param).
                
                # Java parsing is hard. I'll search for the *text* of the method start.
                lines = content.splitlines()
                for i, line in enumerate(lines):
                    if "public void visit(" in line and "{" in line:
                        # Found a visit method start.
                        method_sig = line.strip()
                        # Extract param name
                        match = re.search(r'visit\(\w+\s+(\w+)\)', line)
                        if match:
                            param_name = match.group(1)
                            # Check next few lines for super.visit(param) or visitChildren(param)
                            # We don't parse braces, so we just scan the whole file? No, that's wrong.
                            # We'll scan until the next "public" or EOF?
                            
                            found_call = False
                            for j in range(i + 1, len(lines)):
                                subline = lines[j]
                                if "public " in subline and "class " not in subline and "(" in subline: 
                                    # likely next method
                                    break
                                
                                if f"super.visit({param_name})" in subline or f"visitChildren({param_name})" in subline:
                                    found_call = True
                                    break
                            
                            if not found_call:
                                issues.append(f"{file}: Method '{method_sig}' matches pattern but no strict 'super.visit({param_name})' or 'visitChildren({param_name})' found in approximate body.")

                # Check for instance fields
                # Check for fields that are not final.
                # private boolean foo;
                # private int bar;
                # If they exist, check if they are re-initialized in visit(Actor) or some init?
                field_pattern = re.compile(r'^\s*private\s+(?!final)(static\s+)?[\w<>]+\s+\w+(?:\s*=.*)?;\s*$', re.MULTILINE)
                fields = field_pattern.findall(content)
                if fields:
                     issues.append(f"{file}: Found non-final fields: {fields} - Verify re-initialization.")
                     
    return issues

results = analyze_fixes("/Users/gordon/AntigravityProjects/litterbox/src/main/java/de/uni_passau/fim/se2/litterbox/analytics/fix_heuristics")
for issue in results:
    print(issue)

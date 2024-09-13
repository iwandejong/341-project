package CFG;
import java.util.*;

public class CFG {
    public List<Rule> rules;
    public List<List<Rule>> FIRST; // each rule has its own set (list) of Rules that are terminal and is first to the non-terminal node
    public List<List<Rule>> FOLLOW; // each rule has its own set (list) of Rules that are terminal and follows the first set
    public List<Rule> NULLABLE; // each rule has its own set (list) of Rules that are nullable

    public CFG (List<Rule> _rules) {
        rules = _rules;
        FIRST = buildFIRSTSet();
        // FOLLOW = buildFOLLOWSet();
        NULLABLE = buildNULLABLESet();

        printFIRSTSet();
        // printFOLLOWSet();
        printNULLABLESet();

        // getUniqueRules();
    }

    // TODO: traverse the tokens sequentially
    // when you run into a non-terminal symbol such as GLOBVARS, you "enter" the non-terminal symbol by "expanding" the symbol.
    // in the case of GLOBVARS, you'd enter the symbol and expand to get PROG -> main -> VTYP -> VNAME -> , -> ...
    // if no match is found BUT there is an epsilon-transition (e.g. GLOBVARS -> ε), then continue building the tree.
    // Build the FIRST set for a start.
    public List<List<Rule>> buildFIRSTSet() {
        List<List<Rule>> firstSet = getUniqueRules();
        for (List<Rule> subset : firstSet) {
            List<Rule> f = new ArrayList<Rule>();
            for (Rule rule : rules) {
                if (rule.identifier.equals(subset.get(0).identifier)) {
                    if (!rule.terminal) {
                        // recursively divide and conquer
                        List<Rule> subRules = new ArrayList<Rule>();
                        buildFIRSTSetHelper(subRules, rule.next);
                        f.addAll(subRules);
                    } else {
                        f.add(rule);
                    }
                }
            }

            subset.addAll(f);
        }

        // remove duplicates
        for (List<Rule> subset : firstSet) {
            // remove duplicates in the set (that is the same identifier)
            for (int i = 0; i < subset.size(); i++) {
                Rule r = subset.get(i);
                for (int j = i + 1; j < subset.size(); j++) {
                    if (r.identifier.equals(subset.get(j).identifier)) {
                        subset.remove(j);
                        j--;
                    }
                }
            }
        }

        return firstSet;
    }

    public Rule buildFIRSTSetHelper(List<Rule> subRules, Rule rule) {
        if (rule.terminal) {
            subRules.add(rule);
            return rule;
        }

        // recursively go through until a terminal is found
        for (Rule subRule : rules) {
            if (rule.identifier.equals(subRule.identifier)) {
                // Recursively build FIRST set for sub-rules
                buildFIRSTSetHelper(subRules, subRule.next);
            }
        }
    
        return null;
    }

    public List<List<Rule>> buildFOLLOWSet() {
        List<List<Rule>> followSet = new ArrayList<List<Rule>>();

        // TODO: implement follow set

        return followSet;
    }

    public List<Rule> buildNULLABLESet() {
        List<Rule> nullableSet = new ArrayList<Rule>();
        for (Rule rule : rules) {
            if (rule.next.terminal && rule.next.identifier.equals("ε")) {
                nullableSet.add(rule);
            }
        }

        return nullableSet;
    }

    public List<List<Rule>> getUniqueRules() {
        List<List<Rule>> uniqueRoles = new ArrayList<>();
    
        for (Rule r : rules) {
            boolean exists = false;
    
            for (List<Rule> roleGroup : uniqueRoles) {
                if (roleGroup.get(0).identifier.equals(r.identifier)) {
                    exists = true;
                    break;
                }
            }
    
            if (!exists) {
                List<Rule> newGroup = new ArrayList<>();
                // Add all rules with the same identifier
                for (Rule rule : rules) {
                    if (r.identifier.equals(rule.identifier)) {
                        newGroup.add(rule);
                    }
                }
                uniqueRoles.add(newGroup);
            }
        }
    
        // // Print unique roles
        // for (List<Rule> group : uniqueRoles) {
        //     System.out.println(group.get(0).identifier);
        // }
    
        return uniqueRoles;
    }    

    public void printAllTerminals() {
        for (int i = 0; i < rules.size(); i++) {
            Rule r = rules.get(i);
            while (r != null) {
                if (r.terminal) {
                    System.out.println(r.identifier);
                }
                r = r.next;
            }
        }
    }

    public void printFIRSTSet() {
        System.out.println();
        System.out.println("\u001B[33m" + "FIRST Rules:");
        System.out.println("----------" + "\u001B[0m");
        for (int i = 0; i < FIRST.size(); i++) {
            List<Rule> f = FIRST.get(i);
            String r = "";
            for (int j = 0; j < f.size(); j++) {
                r += f.get(j).identifier;
                if (j != f.size() - 1 && j > 0) {
                    r += ", ";
                }

                if (j == 0) {
                    r += " = { ";
                }
            }
            r += " }";
            System.out.println(r);
        }
    }

    public void printFOLLOWSet() {
        System.out.println();
        System.out.println("\u001B[33m" + "FOLLOW Rules:");
        System.out.println("----------" + "\u001B[0m");
        for (int i = 0; i < FOLLOW.size(); i++) {
            List<Rule> f = FOLLOW.get(i);
            String r = "";
            for (int j = 0; j < f.size(); j++) {
                r += f.get(j).identifier;
                if (j != f.size() - 1 && j > 0) {
                    r += ", ";
                }

                if (j == 0) {
                    r += " = { ";
                }
            }
            r += " }";
            System.out.println(r);
        }
    }

    public void printNULLABLESet() {
        System.out.println();
        System.out.println("\u001B[33m" + "NULLABLE Rules:");
        System.out.println("----------" + "\u001B[0m");
        for (int i = 0; i < NULLABLE.size(); i++) {
            Rule r = NULLABLE.get(i);
            System.out.println(r.identifier);
        }
    }
}

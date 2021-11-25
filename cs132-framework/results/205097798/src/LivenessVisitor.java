import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.DepthFirst;
import cs132.IR.token.Identifier;
import java.util.*;

public class LivenessVisitor extends DepthFirst {
    // s1 and s2 reserved for temporary loads/stores of variables stored on stack
    String[] registers = {"s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t0", "t1", "t2", "t3", "t4", "t5"};
    String[] argRegisters = {"a2", "a3", "a4", "a5", "a6", "a7"};

    // func -> var -> Interval (firstLine, lastLine)
    public HashMap<String, HashMap<String, ArrayList<Integer>>> funcVarInterval = new HashMap<>();
    // func -> var -> reg
    public HashMap<String, HashMap<String, String>> finalRegAssignments = new HashMap<>();
    public HashMap<String, Integer> labels = new HashMap<>();
    public HashSet<ArrayList<Integer>> loops = new HashSet<>();

    private String currFunc;
    private Integer currLine = 1; // relative to a single function
    private HashSet<String> params = new HashSet<>();
    private HashSet<String> visitedVars = new HashSet<>();
    private ArrayList<String> orderedVars = new ArrayList<>();

    // Call this method for saving registers on calls
    public HashSet<String> getRegistersUsedByFunc(String func) {
        Collection<String> nonUniqueRegs = finalRegAssignments.getOrDefault(func, new HashMap<>()).values();
        return new HashSet<>(nonUniqueRegs);
    }

    // Call this method anytime we encounter a variable
    private void updateInterval(String func, String var, Integer line) {
        if (!visitedVars.contains(var)) {
            visitedVars.add(var);
            orderedVars.add(var);
        }
        if (params.contains(var)) {
            return;
        }
        HashMap<String, ArrayList<Integer>> varInterval = funcVarInterval.get(func);
        if (!varInterval.containsKey(var)) {
            ArrayList<Integer> interval = new ArrayList<>();
            interval.add(line); interval.add(line);
            varInterval.put(var, interval);
        } else {
            ArrayList<Integer> interval = varInterval.get(var);
            interval.set(1, line);
        }
    }

    private String getFirstAvailableReg(HashSet<String> usedRegs) {
        for (String r : registers) {
            if (!usedRegs.contains(r)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void visit(FunctionDecl n) {
        currFunc = n.functionName.name;
        currLine = 1;
        params = new HashSet<>();
        labels = new HashMap<>();
        loops = new HashSet<>();
        orderedVars = new ArrayList<>();
        funcVarInterval.put(currFunc, new HashMap<>());
        finalRegAssignments.put(currFunc, new HashMap<>());
        
        // assign params to argument registers
        for (int i = 0; i < n.formalParameters.size(); ++i) {
            String p = n.formalParameters.get(i).toString();
            params.add(p);
            if (i < argRegisters.length) {
                finalRegAssignments.get(currFunc).put(p, argRegisters[i]);
            }
        }

        n.block.accept(this);

        // Set variable interval ends to end of loop if in loop
        for (ArrayList<Integer> i : funcVarInterval.get(currFunc).values()) {
            for (ArrayList<Integer> j : loops) {
                if (i.get(1) >= j.get(0) && i.get(1) <= j.get(1)) {
                    // last usage of variable is within the loop
                    // set live range of variable to end of loop
                    i.set(1, j.get(1));
                }
            }       
        }
        // System.out.println(funcVarInterval.toString());

        // Assign registers
        HashSet<String> usedRegs = new HashSet<>();
        HashMap<String, String> tentativeAssignments = new HashMap<>();
        for (String var : orderedVars) {
            if (params.contains(var)) {
                continue;
            }
            int varStart = funcVarInterval.get(currFunc).get(var).get(0);
            HashSet<String> finalized = new HashSet<>();
            for (String tentativeVar : tentativeAssignments.keySet()) {
                int end = funcVarInterval.get(currFunc).get(tentativeVar).get(1);
                if (end <= varStart) {
                    finalRegAssignments.get(currFunc).put(tentativeVar, tentativeAssignments.get(tentativeVar));
                    usedRegs.remove(tentativeAssignments.get(tentativeVar));
                    finalized.add(tentativeVar);
                    
                }
            }
            for (String v : finalized) {
                tentativeAssignments.remove(v);
            }

            String firstAvailableReg = getFirstAvailableReg(usedRegs);
            if (firstAvailableReg != null) {
                tentativeAssignments.put(var, firstAvailableReg);
                usedRegs.add(firstAvailableReg);
            } else {
                // No more available registers -- evict furthest reaching variable
                String furthestReachingVar = null;
                int furthest = 0;
                for (String tentativeVar : tentativeAssignments.keySet()) {
                    int end = funcVarInterval.get(currFunc).get(tentativeVar).get(1);
                    if (end > furthest) {
                        furthest = end;
                        furthestReachingVar = tentativeVar;
                    }
                }
                usedRegs.remove(tentativeAssignments.get(furthestReachingVar));
                tentativeAssignments.remove(furthestReachingVar);

                firstAvailableReg = getFirstAvailableReg(usedRegs);
                tentativeAssignments.put(var, firstAvailableReg);
            }
        }

        finalRegAssignments.get(currFunc).putAll(tentativeAssignments);
    }

    @Override
    public void visit(Block n) {
        for (Instruction i : n.instructions) {
            currLine++;
            i.accept(this);
        }
        currLine++;
        updateInterval(currFunc, n.return_id.toString(), currLine);
    }

    @Override
    public void visit(LabelInstr n) {
        labels.put(n.label.toString(), currLine);
    }

    @Override
    public void visit(Goto n) {
        String label = n.label.toString();
        if (labels.containsKey(label) && labels.get(label) < currLine) {
            Integer a[] = new Integer[]{labels.get(label), currLine};
            ArrayList<Integer> interval = new ArrayList<>(Arrays.asList(a));
            loops.add(interval);
        }
    }

    @Override
    public void visit(IfGoto n) {
        String label = n.label.toString();
        if (labels.containsKey(label) && labels.get(label) < currLine) {
            Integer a[] = new Integer[]{labels.get(label), currLine};
            ArrayList<Integer> interval = new ArrayList<>(Arrays.asList(a));
            loops.add(interval);
        }
        updateInterval(currFunc, n.condition.toString(), currLine);
    }

    @Override
    public void visit(Move_Id_Integer n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
    }

    @Override
    public void visit(Move_Id_FuncName n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
    }

    @Override
    public void visit(Move_Id_Id n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.rhs.toString(), currLine);
    }

    @Override
    public void visit(Add n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.arg1.toString(), currLine);
        updateInterval(currFunc, n.arg2.toString(), currLine);
    }

    @Override
    public void visit(Subtract n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.arg1.toString(), currLine);
        updateInterval(currFunc, n.arg2.toString(), currLine);
    }

    @Override
    public void visit(Multiply n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.arg1.toString(), currLine);
        updateInterval(currFunc, n.arg2.toString(), currLine);
    }

    @Override
    public void visit(LessThan n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.arg1.toString(), currLine);
        updateInterval(currFunc, n.arg2.toString(), currLine);
    }

    @Override
    public void visit(Load n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.base.toString(), currLine);
    }

    @Override
    public void visit(Store n) {
        updateInterval(currFunc, n.rhs.toString(), currLine);
        updateInterval(currFunc, n.base.toString(), currLine);
    }

    @Override
    public void visit(Alloc n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.size.toString(), currLine);
    }

    @Override
    public void visit(Print n) {
        updateInterval(currFunc, n.content.toString(), currLine);
    }

    @Override
    public void visit(Call n) {
        updateInterval(currFunc, n.lhs.toString(), currLine);
        updateInterval(currFunc, n.callee.toString(), currLine);
        for (Identifier i : n.args) {
            updateInterval(currFunc, i.toString(), currLine);
        }
    }
}
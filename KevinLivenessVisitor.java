import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.DepthFirst;
import cs132.IR.token.Identifier;

import java.util.*;

class Interval {
    Integer start;
    Integer end;

    public Interval(int start) {
        this.start = start;
        this.end = start;
    }

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", start, end);
    }
}

public class KevinLivenessVisitor extends DepthFirst {
//    String[] registers = {"s3", "s4", "s5", "s6"};
    String[] registers = {"s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t0", "t1", "t2", "t3", "t4", "t5"};
    String[] argRegisters = {"a2", "a3", "a4", "a5", "a6", "a7"};

    // {"functionName": {"varName": "register"}}
    HashMap<String, HashMap<String, String>> regAssignments = new HashMap<>();
    HashMap<String, HashMap<String, Interval>> allVarIntervals = new HashMap<>();

    String curFunc;
    Integer curLine = 0;

    // {"labelName": line # of label}
    HashMap<String, Integer> labelLocations = new HashMap<>();
    // {"varName", Interval}
    HashMap<String, Interval> varIntervals = new HashMap<>();
    HashSet<Interval> loops = new HashSet<>();
    HashSet<String> params = new HashSet<>();

    @Override
    public void visit(FunctionDecl n) {
        curFunc = n.functionName.name;

        regAssignments.put(curFunc, new HashMap<>());
        allVarIntervals.put(curFunc, new HashMap<>());

        curLine = 1;
        varIntervals = new HashMap<>();
        labelLocations = new HashMap<>();
        loops = new HashSet<>();
        params = new HashSet<>();

        for (int i = 0; i < n.formalParameters.size(); ++i) {
            String p = n.formalParameters.get(i).toString();
            params.add(p);
            if (i < argRegisters.length) {
                regAssignments.get(curFunc).put(p, argRegisters[i]);
            }
        }
        n.block.accept(this);

        // Update intervals with loops
        for (Interval loopInterval : loops) {
            for (Interval varInterval : varIntervals.values()) {
                if (varInterval.end >= loopInterval.start && varInterval.end < loopInterval.end) {
                    varInterval.end = loopInterval.end;
                }
            }
        }

        // Assign registers based on intervals
        HashMap<String, String> funcRegAssignments = new HashMap<>();
        HashMap<String, String> regToVar = new HashMap<>();
        List<Map.Entry<String, Interval>> varStartsSorted = new ArrayList<>(varIntervals.entrySet());
        Collections.sort(varStartsSorted, (Map.Entry<String, Interval> a, Map.Entry<String, Interval> b) -> a.getValue().start - b.getValue().start);
        List<Map.Entry<String, Interval>> varEndsSorted = new ArrayList<>();
        ArrayList<String> availableRegs = new ArrayList<>(Arrays.asList(registers));

        for (Map.Entry<String, Interval> e : varStartsSorted) {
            String varName = e.getKey();
            Integer varStart = e.getValue().start;
            Integer varEnd = e.getValue().end;
            while (varEndsSorted.size() > 0 && varEndsSorted.get(0).getValue().end <= varStart) {
                Map.Entry<String, Interval> v = varEndsSorted.remove(0);
                String freedReg = funcRegAssignments.get(v.getKey());
                regToVar.remove(freedReg);
                availableRegs.add(freedReg);
            }
            if (availableRegs.size() == 0) {
                Map.Entry<String, Interval> v = varEndsSorted.remove(0);
                String freedReg = funcRegAssignments.remove(v.getKey());
                regToVar.remove(freedReg);
                availableRegs.add(freedReg);
            }
            if (availableRegs.size() > 0) {
                String reg = availableRegs.remove(0);
                funcRegAssignments.put(varName, reg);
                regToVar.put(reg, varName);
                varEndsSorted.add(new AbstractMap.SimpleEntry<String, Interval>(varName, new Interval(varEnd)));
                Collections.sort(varEndsSorted, (Map.Entry<String, Interval> a, Map.Entry<String, Interval> b) -> a.getValue().end - b.getValue().end);
            } else {
                System.out.println("ERROR");
                System.exit(1);
            }
        }

        regAssignments.get(curFunc).putAll(funcRegAssignments);
        // regAssignments.get(curFunc).clear();
    //    regAssignments.put(curFunc, funcRegAssignments);
        allVarIntervals.put(curFunc, varIntervals);
    }

    @Override
    public void visit(Block n) {
        for (Instruction i : n.instructions) {
            i.accept(this);
            curLine++;
        }
        setInterval(n.return_id);
    }

    @Override
    public void visit(LabelInstr n) {
        labelLocations.put(n.label.toString(), curLine);
    }

    @Override
    public void visit(Goto n) {
        String label = n.label.toString();
        if (labelLocations.containsKey(label) && labelLocations.get(label) < curLine) {
            loops.add(new Interval(labelLocations.get(label), curLine));
        }
    }

    @Override
    public void visit(IfGoto n) {
        String label = n.label.toString();
        if (labelLocations.containsKey(label) && labelLocations.get(label) < curLine) {
            loops.add(new Interval(labelLocations.get(label), curLine));
        }
        setInterval(n.condition);
    }

    @Override
    public void visit(Move_Id_Integer n) {
        setInterval(n.lhs);
    }

    @Override
    public void visit(Move_Id_FuncName n) {
        setInterval(n.lhs);
    }

    @Override
    public void visit(Move_Id_Id n) {
        setInterval(n.lhs); setInterval(n.rhs);
    }

    @Override
    public void visit(Add n) {
        setInterval(n.lhs); setInterval(n.arg1); setInterval(n.arg2);
    }

    @Override
    public void visit(Subtract n) {
        setInterval(n.lhs); setInterval(n.arg1); setInterval(n.arg2);
    }

    @Override
    public void visit(Multiply n) {
        setInterval(n.lhs); setInterval(n.arg1); setInterval(n.arg2);
    }

    @Override
    public void visit(LessThan n) {
        setInterval(n.lhs); setInterval(n.arg1); setInterval(n.arg2);
    }

    @Override
    public void visit(Load n) {
        setInterval(n.lhs); setInterval(n.base);
    }

    @Override
    public void visit(Store n) {
        setInterval(n.base); setInterval(n.rhs);
    }

    @Override
    public void visit(Alloc n) {
        setInterval(n.lhs); setInterval(n.size);
    }

    @Override
    public void visit(Print n) {
        setInterval(n.content);
    }

    @Override
    public void visit(Call n) {
        setInterval(n.lhs);
        setInterval(n.callee);
        for (Identifier i : n.args) {
            setInterval(i);
        }
    }

    // ============================================================================================
    // MARK: Helpers
    // ============================================================================================

    void setInterval(Identifier var) {
        String name = var.toString();
        if (!params.contains(name)) {
            if (varIntervals.containsKey(name)) {
                varIntervals.get(name).end = Math.max(curLine, varIntervals.get(name).end);
            } else {
                varIntervals.put(name, new Interval(curLine));
            }
        }
    }
}

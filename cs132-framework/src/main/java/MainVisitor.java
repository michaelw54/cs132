import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.ArgVisitor;
import cs132.IR.token.Identifier;
import java.util.ArrayList;
import java.util.HashSet;

public class MainVisitor implements ArgVisitor<LivenessVisitor> {
    String currFunc;
    Integer currLine = 1;

    @Override
    public void visit(Program arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        for (FunctionDecl f : arg0.funDecls) {
            f.accept(this, arg1);
        }
    }

    @Override
    public void visit(FunctionDecl arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        currFunc = arg0.functionName.name;
        currLine = 1;
        ArrayList<String> extraArgs = new ArrayList<>();
        for (int i = 6; i < arg0.formalParameters.size(); i++) {
            extraArgs.add(arg0.formalParameters.get(i).toString());
        }
        if (extraArgs.size() > 0) {
            System.out.println("func " + currFunc + "(" + String.join(" ", extraArgs) + ")");
        } else {
            System.out.println("func " + currFunc + "()");
        }
        arg0.block.accept(this, arg1);
        System.out.println();
    }

    @Override
    public void visit(Block arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        for (Instruction i : arg0.instructions) {
            currLine++;
            i.accept(this, arg1);
        }
        currLine++;
        String ret = arg0.return_id.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(ret)) {
            System.out.println(ret + " = " + arg1.finalRegAssignments.get(currFunc).get(ret));
        }
        System.out.println("return " + ret);
    }

    @Override
    public void visit(LabelInstr arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        System.out.println(arg0.toString());
    }

    @Override
    public void visit(Move_Id_Integer arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String rhs = Integer.toString(arg0.rhs);
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = " + rhs);
        } else {
            System.out.println("s1 = " + rhs);
            System.out.println(lhs + " = s1");
        }
    }

    @Override
    public void visit(Move_Id_FuncName arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String rhs = arg0.rhs.name;
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = @" + rhs);
        } else {
            System.out.println("s1 = @" + rhs);
            System.out.println(lhs + " = s1");
        }
    }

    @Override
    public void visit(Add arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String a1 = arg0.arg1.toString();
        String a2 = arg0.arg2.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a1)) {
            a1 = arg1.finalRegAssignments.get(currFunc).get(a1);
        } else {
            System.out.println("s1 = " + a1);
            a1 = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a2)) {
            a2 = arg1.finalRegAssignments.get(currFunc).get(a2);
        } else {
            System.out.println("s2 = " + a2);
            a2 = "s2";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = " + a1 + " + " + a2);
        } else {
            System.out.println("s1 = " + a1 + " + " + a2);
            System.out.println(lhs + " = s1");
        }
    }

    @Override
    public void visit(Subtract arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String a1 = arg0.arg1.toString();
        String a2 = arg0.arg2.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a1)) {
            a1 = arg1.finalRegAssignments.get(currFunc).get(a1);
        } else {
            System.out.println("s1 = " + a1);
            a1 = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a2)) {
            a2 = arg1.finalRegAssignments.get(currFunc).get(a2);
        } else {
            System.out.println("s2 = " + a2);
            a2 = "s2";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = " + a1 + " - " + a2);
        } else {
            System.out.println("s1 = " + a1 + " - " + a2);
            System.out.println(lhs + " = s1");
        }
    }

    @Override
    public void visit(Multiply arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String a1 = arg0.arg1.toString();
        String a2 = arg0.arg2.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a1)) {
            a1 = arg1.finalRegAssignments.get(currFunc).get(a1);
        } else {
            System.out.println("s1 = " + a1);
            a1 = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a2)) {
            a2 = arg1.finalRegAssignments.get(currFunc).get(a2);
        } else {
            System.out.println("s2 = " + a2);
            a2 = "s2";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = " + a1 + " * " + a2);
        } else {
            System.out.println("s1 = " + a1 + " * " + a2);
            System.out.println(lhs + " = s1");
        }
        // System.out.println(lhs + " : " + a1 + " : " + a2);
    }

    @Override
    public void visit(LessThan arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String a1 = arg0.arg1.toString();
        String a2 = arg0.arg2.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a1)) {
            a1 = arg1.finalRegAssignments.get(currFunc).get(a1);
        } else {
            System.out.println("s1 = " + a1);
            a1 = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(a2)) {
            a2 = arg1.finalRegAssignments.get(currFunc).get(a2);
        } else {
            System.out.println("s2 = " + a2);
            a2 = "s2";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = " + a1 + " < " + a2);
        } else {
            System.out.println("s1 = " + a1 + " < " + a2);
            System.out.println(lhs + " = s1");
        }
    }

    @Override
    public void visit(Load arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String base = arg0.base.toString();
        String offset = Integer.toString(arg0.offset);
        if (arg1.finalRegAssignments.get(currFunc).containsKey(base)) {
            base = arg1.finalRegAssignments.get(currFunc).get(base);
        } else {
            System.out.println("s1 = " + base);
            base = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = [" + base + " + " + offset + "]");
        } else {
            System.out.println("s1 = [" + base + " + " + offset + "]");
            System.out.println(lhs + " = s1");
        }
    }

    @Override
    public void visit(Store arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String rhs = arg0.rhs.toString();
        String base = arg0.base.toString();
        String offset = Integer.toString(arg0.offset);
        if (arg1.finalRegAssignments.get(currFunc).containsKey(base)) {
            base = arg1.finalRegAssignments.get(currFunc).get(base);
        } else {
            System.out.println("s1 = " + base);
            base = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(rhs)) {
            rhs = arg1.finalRegAssignments.get(currFunc).get(rhs);
        } else {
            System.out.println("s2 = " + rhs);
            rhs = "s2";
        }
        System.out.println("[" + base + " + " + offset + "] = " + rhs);
    }

    @Override
    public void visit(Move_Id_Id arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String rhs = arg0.rhs.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(rhs)) {
            rhs = arg1.finalRegAssignments.get(currFunc).get(rhs);
        } else {
            System.out.println("s1 = " + rhs);
            rhs = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = " + rhs);
        } else {
            System.out.println(lhs + " = " + rhs);
        }
    }

    @Override
    public void visit(Alloc arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String size = arg0.size.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(size)) {
            size = arg1.finalRegAssignments.get(currFunc).get(size);
        } else {
            System.out.println("s1 = " + size);
            size = "s1";
        }
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            System.out.println(arg1.finalRegAssignments.get(currFunc).get(lhs) + " = alloc(" + size + ")");
        } else {
            System.out.println("s2 = alloc(" + size + ")");
            System.out.println(lhs + " = s2");
        }
    }

    @Override
    public void visit(Print arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String content = arg0.content.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(content)) {
            System.out.println("print(" + arg1.finalRegAssignments.get(currFunc).get(content) + ")");
        } else {
            System.out.println("s1 = " + content);
            System.out.println("print(s1)");
        }
    }

    @Override
    public void visit(ErrorMessage arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String err = arg0.msg.toString();
        System.out.println("error(" + err + ")");
    }

    @Override
    public void visit(Goto arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String label = arg0.label.toString();
        System.out.println("goto " + label);
    }

    @Override
    public void visit(IfGoto arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String cond = arg0.condition.toString();
        String label = arg0.label.toString();
        if (arg1.finalRegAssignments.get(currFunc).containsKey(cond)) {
            System.out.println("if0 " + arg1.finalRegAssignments.get(currFunc).get(cond) + " goto " + label);
        } else {
            System.out.println("s1 = " + cond);
            System.out.println("if0 s1 goto " + label);
        }
    }

    @Override
    public void visit(Call arg0, LivenessVisitor arg1) {
        // TODO Auto-generated method stub
        String lhs = arg0.lhs.toString();
        String callee = arg0.callee.toString();
        int numArgs = 0;
        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> argRegAssignmentStatements = new ArrayList<>();
        Boolean lhsIsRegister = false;

        // TEMP
        // TODO: optimize register saving
        // only save registers that are used by variables with
        // liveness ranges containing line # of the call instruction
        if (arg1.finalRegAssignments.get(currFunc).containsKey(lhs)) {
            lhsIsRegister = true;
            lhs = arg1.finalRegAssignments.get(currFunc).get(lhs);
        }

        // V3
        HashSet<String> regsToSave = new HashSet<>();
        for (String var : arg1.funcVarInterval.get(currFunc).keySet()) {
            ArrayList<Integer> interval = arg1.funcVarInterval.get(currFunc).get(var);
            if (currLine >= interval.get(0) && currLine <= interval.get(1)) {
                if (arg1.finalRegAssignments.get(currFunc).containsKey(var)) {
                    regsToSave.add(arg1.finalRegAssignments.get(currFunc).get(var));
                }
            }
        }
        for (String reg : regsToSave) {
            if (!reg.startsWith("a") && !reg.equals(lhs)) {
                System.out.println("save_reg_" + reg + " = " + reg);
            }
        }
        for (String reg : arg1.getRegistersUsedByFunc(currFunc)) {
            if (reg.startsWith("a") && !reg.equals(lhs)) {
                System.out.println("save_reg_" + reg + " = " + reg);
            }
        }

        // V2
        // for (String reg : arg1.getRegistersUsedByFunc(currFunc)) {
        //     if (!reg.equals(lhs)) {
        //         System.out.println("save_reg_" + reg + " = " + reg);
        //     }
        // }

        // V1
        // for (String reg : arg1.registers) {
        //    System.out.println("save_reg_" + reg + " = " + reg);
        // }
        // for (String reg : arg1.argRegisters) {
        //     System.out.println("save_reg_" + reg + " = " + reg);
        // }

        // put arguments on stack as identifiers if > 6 arguments
        for (Identifier i : arg0.args) {
            if (numArgs >= 6) {
                if (arg1.finalRegAssignments.get(currFunc).containsKey(i.toString())) {
                    System.out.println(i.toString() + " = " + arg1.finalRegAssignments.get(currFunc).get(i.toString()));
                }
                args.add(i.toString());
            } else {
                if (arg1.finalRegAssignments.get(currFunc).containsKey(i.toString())) {
                    argRegAssignmentStatements.add("a" + Integer.toString(numArgs+2) + " = " + arg1.finalRegAssignments.get(currFunc).get(i.toString()));
                } else {
                    argRegAssignmentStatements.add("a" + Integer.toString(numArgs+2) + " = " + i.toString());
                }
            }
            numArgs++;
        }

        // put arguments in arg registers
        for (String assignStmt : argRegAssignmentStatements) {
            System.out.println(assignStmt);
        }
        String stringifiedArgs = String.join(" ", args);

        if (arg1.finalRegAssignments.get(currFunc).containsKey(callee)) {
            callee = arg1.finalRegAssignments.get(currFunc).get(callee);
        } else {
            System.out.println("s1 = " + callee);
            callee = "s1";
        }

        if (lhsIsRegister) {
            System.out.println(lhs + " = call " + callee + "(" + stringifiedArgs + ")");
        } else {
            System.out.println("s2 = call " + callee + "(" + stringifiedArgs + ")");
            System.out.println(lhs + " = s2");
        }

        // TEMP
        // TODO: optimize register saving

        // V3
        for (String reg : regsToSave) {
            if (!reg.startsWith("a") && !reg.equals(lhs)) {
                System.out.println(reg + " = save_reg_" + reg);
           }
        }
        for (String reg : arg1.getRegistersUsedByFunc(currFunc)) {
            if (reg.startsWith("a") && !reg.equals(lhs)) {
                System.out.println(reg + " = save_reg_" + reg);
            }
        }

        // V2
        // for (String reg : arg1.getRegistersUsedByFunc(currFunc)) {
        //     if (!reg.equals(lhs)) {
        //         System.out.println(reg + " = save_reg_" + reg);
        //    }
        // }

        // V1
    //    for (String reg : arg1.registers) {
    //        if (!reg.equals(lhs)) {
    //             System.out.println(reg + " = save_reg_" + reg);
    //        }
    //    }
    //    for (String reg : arg1.argRegisters) {
    //         System.out.println(reg + " = save_reg_" + reg);
    //    }
    }

}

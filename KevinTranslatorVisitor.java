import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.ArgVisitor;
import cs132.IR.token.Identifier;

import java.awt.image.AreaAveragingScaleFilter;
import java.lang.reflect.Array;
import java.util.*;

public class KevinTranslatorVisitor implements ArgVisitor<KevinLivenessVisitor> {

    String curFunc;

    ArrayList<String> result = new ArrayList<>();

    @Override
    public void visit(Program program, KevinLivenessVisitor livenessVisitor) {
        for (FunctionDecl f : program.funDecls) {
            f.accept(this, livenessVisitor);
        }
    }

    @Override
    public void visit(FunctionDecl functionDecl, KevinLivenessVisitor livenessVisitor) {
        curFunc = functionDecl.functionName.name;
        ArrayList<String> args = new ArrayList<>();
        for (Identifier i : functionDecl.formalParameters) {
            if (!livenessVisitor.regAssignments.get(curFunc).containsKey(i.toString())) {
                args.add(i.toString());
            }
        }
        result.add("func " + curFunc + "(" + String.join(" ", args) + ")");
        functionDecl.block.accept(this, livenessVisitor);
        result.add("");
    }

    @Override
    public void visit(Block block, KevinLivenessVisitor livenessVisitor) {
        String return_id = block.return_id.toString();
        for (Instruction i : block.instructions) {
            i.accept(this, livenessVisitor);
        }

        if (livenessVisitor.regAssignments.get(curFunc).containsKey(return_id)) {
            result.add(return_id + " = " + livenessVisitor.regAssignments.get(curFunc).get(return_id));
        }
        result.add("return " + block.return_id.toString());
    }

    @Override
    public void visit(LabelInstr labelInstr, KevinLivenessVisitor livenessVisitor) {
        result.add(labelInstr.toString());
    }

    @Override
    public void visit(Move_Id_Integer move_id_integer, KevinLivenessVisitor livenessVisitor) {
        String lhs = move_id_integer.lhs.toString();
        int rhs = move_id_integer.rhs;
        if (livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = " + rhs);
        } else {
            result.add("s1 = " + rhs);
            result.add(lhs + " = s1");
        }
    }

    @Override
    public void visit(Move_Id_FuncName move_id_funcName, KevinLivenessVisitor livenessVisitor) {
        String lhs = move_id_funcName.lhs.toString();
        String rhs = move_id_funcName.rhs.toString();
        if (livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = @" + rhs);
        } else {
            result.add("s1 = @" + rhs);
            result.add(lhs + " = s1");
        }
    }

    @Override
    public void visit(Add add, KevinLivenessVisitor livenessVisitor) {
        String lhs = add.lhs.toString();
        String arg1 = add.arg1.toString();
        String arg2 = add.arg2.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg1)) {
            result.add("s1 = " + arg1);
            arg1 = "s1";
        } else {
            arg1 = livenessVisitor.regAssignments.get(curFunc).get(arg1);
        }
        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg2)) {
            result.add("s2 = " + arg2);
            arg2 = "s2";
        } else {
            arg2 = livenessVisitor.regAssignments.get(curFunc).get(arg2);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s1 = " + arg1 + " + " + arg2);
            result.add(lhs + " = s1");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = " + arg1 + " + " + arg2);
        }
    }

    @Override
    public void visit(Subtract subtract, KevinLivenessVisitor livenessVisitor) {
        String lhs = subtract.lhs.toString();
        String arg1 = subtract.arg1.toString();
        String arg2 = subtract.arg2.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg1)) {
            result.add("s1 = " + arg1);
            arg1 = "s1";
        } else {
            arg1 = livenessVisitor.regAssignments.get(curFunc).get(arg1);
        }
        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg2)) {
            result.add("s2 = " + arg2);
            arg2 = "s2";
        } else {
            arg2 = livenessVisitor.regAssignments.get(curFunc).get(arg2);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s1 = " + arg1 + " - " + arg2);
            result.add(lhs + " = s1");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = " + arg1 + " - " + arg2);
        }
    }

    @Override
    public void visit(Multiply multiply, KevinLivenessVisitor livenessVisitor) {
        String lhs = multiply.lhs.toString();
        String arg1 = multiply.arg1.toString();
        String arg2 = multiply.arg2.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg1)) {
            result.add("s1 = " + arg1);
            arg1 = "s1";
        } else {
            arg1 = livenessVisitor.regAssignments.get(curFunc).get(arg1);
        }
        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg2)) {
            result.add("s2 = " + arg2);
            arg2 = "s2";
        } else {
            arg2 = livenessVisitor.regAssignments.get(curFunc).get(arg2);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s1 = " + arg1 + " * " + arg2);
            result.add(lhs + " = s1");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = " + arg1 + " * " + arg2);
        }
    }

    @Override
    public void visit(LessThan lessThan, KevinLivenessVisitor livenessVisitor) {
        String lhs = lessThan.lhs.toString();
        String arg1 = lessThan.arg1.toString();
        String arg2 = lessThan.arg2.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg1)) {
            result.add("s1 = " + arg1);
            arg1 = "s1";
        } else {
            arg1 = livenessVisitor.regAssignments.get(curFunc).get(arg1);
        }
        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(arg2)) {
            result.add("s2 = " + arg2);
            arg2 = "s2";
        } else {
            arg2 = livenessVisitor.regAssignments.get(curFunc).get(arg2);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s1 = " + arg1 + " < " + arg2);
            result.add(lhs + " = s1");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = " + arg1 + " < " + arg2);
        }
    }

    @Override
    public void visit(Load load, KevinLivenessVisitor livenessVisitor) {
        String lhs = load.lhs.toString();
        String base = load.base.toString();
        int offset = load.offset;

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(base)) {
            result.add("s1 = " + base);
            base = "s1";
        } else {
            base = livenessVisitor.regAssignments.get(curFunc).get(base);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s2 = [" + base + " + " + offset + "]");
            result.add(lhs + " = s2");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = [" + base + " + " + offset + "]");
        }
    }

    @Override
    public void visit(Store store, KevinLivenessVisitor livenessVisitor) {
        String base = store.base.toString();
        int offset = store.offset;
        String rhs = store.rhs.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(base)) {
            result.add("s1 = " + base);
            base = "s1";
        } else {
            base = livenessVisitor.regAssignments.get(curFunc).get(base);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(rhs)) {
            result.add("s2 = " + rhs);
            rhs = "s2";
        } else {
            rhs = livenessVisitor.regAssignments.get(curFunc).get(rhs);
        }
        result.add("[" + base + " + " + offset + "] = " + rhs);
    }

    @Override
    public void visit(Move_Id_Id move_id_id, KevinLivenessVisitor livenessVisitor) {
        String lhs = move_id_id.lhs.toString();
        String rhs = move_id_id.rhs.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(rhs) && !livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s1 = " + rhs);
            result.add(lhs + " = s1");
        } else if (!livenessVisitor.regAssignments.get(curFunc).containsKey(rhs) && livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s1 = " + rhs);
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = s1");
        } else if (livenessVisitor.regAssignments.get(curFunc).containsKey(rhs) && !livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add(lhs + " = " + livenessVisitor.regAssignments.get(curFunc).get(rhs));
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = " + livenessVisitor.regAssignments.get(curFunc).get(rhs));
        }
    }

    @Override
    public void visit(Alloc alloc, KevinLivenessVisitor livenessVisitor) {
        String lhs = alloc.lhs.toString();
        String size = alloc.size.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(size)) {
            result.add("s1 = " + size);
            size = "s1";
        } else {
            size = livenessVisitor.regAssignments.get(curFunc).get(size);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s2 = alloc(" + size + ")");
            result.add(lhs + " = s2");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = alloc(" + size + ")");
        }
    }

    @Override
    public void visit(Print print, KevinLivenessVisitor livenessVisitor) {
        String content = print.content.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(content)) {
            result.add("s1 = " + content);
            result.add("print(s1)");
        } else {
            result.add("print(" + livenessVisitor.regAssignments.get(curFunc).get(content) + ")");
        }
    }

    @Override
    public void visit(ErrorMessage errorMessage, KevinLivenessVisitor livenessVisitor) {
        result.add(errorMessage.toString());
    }

    @Override
    public void visit(Goto aGoto, KevinLivenessVisitor livenessVisitor) {
        result.add(aGoto.toString());
    }

    @Override
    public void visit(IfGoto ifGoto, KevinLivenessVisitor livenessVisitor) {
        String condition = ifGoto.condition.toString();
        String label = ifGoto.label.toString();

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(condition)) {
            result.add("s1 = " + condition);
            result.add("if0 s1 goto " + label);
        } else {
            result.add("if0 " + livenessVisitor.regAssignments.get(curFunc).get(condition) + " goto " + label);
        }
    }

    @Override
    public void visit(Call call, KevinLivenessVisitor livenessVisitor) {
        String lhs = call.lhs.toString();
        String callee = call.callee.toString();

        // for (String reg : livenessVisitor.registers) {
        //    result.add("save_reg_" + reg + " = " + reg);
        // }
        // for (String reg : livenessVisitor.argRegisters) {
        //     result.add("save_reg_" + reg + " = " + reg);
        // }

        ArrayList<String> args = new ArrayList<>();
        int c = 2;

        List<String> argRegs = new ArrayList<>();
        for (Identifier arg: call.args) {
            String argName = arg.toString();
            if (c < 8) {
                argRegs.add(livenessVisitor.regAssignments.get(curFunc).getOrDefault(argName, argName));
                c++; // haha
            } else {
                if (livenessVisitor.regAssignments.get(curFunc).containsKey(argName)) {
                    result.add(argName + " = " + livenessVisitor.regAssignments.get(curFunc).get(argName));
                }
                args.add(argName);
            }
        }
        for (int i = 0; i < argRegs.size(); i++) {
            result.add("a" + (i+2) + " = " + argRegs.get(i));
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(callee)) {
            result.add("s1 = " + callee);
            callee = "s1";
        } else {
            callee = livenessVisitor.regAssignments.get(curFunc).get(callee);
        }

        if (!livenessVisitor.regAssignments.get(curFunc).containsKey(lhs)) {
            result.add("s2 = call " + callee + "(" + String.join(" ", args) + ")");
            result.add(lhs + " = s2");
        } else {
            result.add(livenessVisitor.regAssignments.get(curFunc).get(lhs) + " = call " + callee + "(" + String.join(" ", args) + ")");
        }

    //    for (String reg : livenessVisitor.registers) {
    //        if (!reg.equals(lhs)) {
    //             result.add(reg + " = save_reg_" + reg);
    //        }
    //    }
    //    for (String reg : livenessVisitor.argRegisters) {
    //         result.add(reg + " = save_reg_" + reg);
    //    }
    }

    public void addSaves() {
        ArrayList<String> newResult = new ArrayList<>();

        String[] allRegsPrimitive = {"s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t0", "t1", "t2", "t3", "t4", "t5", "a2", "a3", "a4", "a5", "a6", "a7"};
//        String[] allRegsPrimitive = {"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t0", "t1", "t2", "t3", "t4", "t5", "a2", "a3", "a4", "a5", "a6", "a7"};
        List<String> allRegs = Arrays.asList(allRegsPrimitive);

        while (result.size() > 0) {
            ArrayList<String> funcResult = new ArrayList<>();
            while (result.get(0).length() > 0) {
                funcResult.add(result.remove(0));
            }
            result.remove(0);
            while (true) {
                int argIdx = 99999999;
                int callIdx = 99999999;
                HashSet<String> regsNotToSave = new HashSet<>();
                HashSet<String> regsToSave = new HashSet<>();
                for (int i = 0; i < funcResult.size(); ++i) {
                    String line = funcResult.get(i);
                    if (line.startsWith("a") && Character.isDigit(line.charAt(1)) && line.charAt(3) == '=' && argIdx == 99999999) {
                        argIdx = i;
                    }
                    if (line.contains("call") && line.contains("(") && callIdx == 99999999) {
                        callIdx = i;
                        argIdx = Math.min(argIdx, callIdx);
                        if (line.charAt(3) == '=' && Character.isDigit(line.charAt(1))) {
                            regsNotToSave.add(line.substring(0, 2));
                        } else if (line.charAt(4) == '=' && Character.isDigit(line.charAt(2))) {
                            regsNotToSave.add(line.substring(0, 3));
                        }
                        continue;
                    }
                    if (callIdx < 99999999) {
                        for (String reg : allRegs) {
                            if (line.substring(2).contains(reg) && !regsNotToSave.contains(reg)) {
                                regsToSave.add(reg);
                            }
                        }
                        String firstTwoChars = line.substring(0, 2);
                        if (allRegs.contains(firstTwoChars) && !regsToSave.contains(firstTwoChars)) {
                            regsNotToSave.add(firstTwoChars);
                        } else {
                            String firstThreeChars = line.substring(0, 3);
                            if (allRegs.contains(firstTwoChars) && !regsToSave.contains(firstTwoChars)) {
                                regsNotToSave.add(firstTwoChars);
                            }
                        }
                    }
                }
                if (callIdx == 99999999) { break; }
                for (String r : regsToSave) {
                    funcResult.add(callIdx + 1, r + " = save_reg_" + r);
                }
                for (String r : regsToSave) {
                    funcResult.add(argIdx, "save_reg_" + r + " = " + r);
                }
                newResult.addAll(funcResult.subList(0, 1+callIdx+(2*regsToSave.size())));
                funcResult = new ArrayList<String>(funcResult.subList(1+callIdx+(2*regsToSave.size()), funcResult.size()));
            }
            newResult.addAll(funcResult);
            newResult.add("");
        }

        result = newResult;
    }
}

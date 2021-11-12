import cs132.minijava.visitor.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.syntaxtree.Block;
import cs132.minijava.syntaxtree.Identifier;
import cs132.minijava.syntaxtree.IntegerLiteral;
import cs132.minijava.syntaxtree.Node;
import cs132.minijava.syntaxtree.NodeList;
import cs132.minijava.syntaxtree.NodeListOptional;
import cs132.minijava.syntaxtree.NodeOptional;
import cs132.minijava.syntaxtree.NodeSequence;
import cs132.minijava.syntaxtree.NodeToken;
import cs132.IR.syntaxtree.*;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.HashMap;

public class MainVisitor extends GJVoidDepthFirst<Scope> {

    private String createVar(int k) {
        return "w" + String.valueOf(k);
    }

    private boolean isArray(String arrayType) {
        return arrayType.substring(Math.max(arrayType.length() - 2, 0)).equals("[]");
    }

    private String getArrayType(String arrayType) {
        return arrayType.substring(0, arrayType.length() - 2);
    }

    private void printWithIndents(String msg, Scope arg1) {
        if (arg1.scope.equals("method")) {
            System.out.println("    " + msg);
        } else {
            System.out.println(msg);
        }
    }

    private void printLabel(String label) {
        System.out.println("  " + label + ":");
    }

    @Override
    public void visit(Goal arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
        arg0.f1.accept(this, arg1);
        arg0.f2.accept(this, arg1);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public void visit(MainClass arg0, Scope arg1) {
        printWithIndents("func Main()", arg1);
        arg1.scope = "method";
        arg0.f14.accept(this, arg1);
        arg0.f15.accept(this, arg1);
        printWithIndents("ret_ = 0", arg1);
        printWithIndents("return ret_", arg1);
        printWithIndents("", arg1);
        arg1.stepOut();
    }

    @Override
    public void visit(TypeDeclaration arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    @Override
    public void visit(ClassDeclaration arg0, Scope arg1) {
        arg1.scope = "class";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        arg0.f3.accept(this, arg1);
        arg0.f4.accept(this, arg1);
        // printWithIndents("Class: " + arg1.currentClass);
        arg1.stepOut();
    }

    @Override
    public void visit(ClassExtendsDeclaration arg0, Scope arg1) {
        arg1.scope = "class";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        arg0.f5.accept(this, arg1);
        arg0.f6.accept(this, arg1);
        // printWithIndents("Class: " + arg1.currentClass);
        arg1.stepOut();
    }

    @Override
    public void visit(VarDeclaration arg0, Scope arg1) {
        TypecheckVisitor visitor = new TypecheckVisitor();
        if (arg1.scope.equals("class")) {
            // TODO
        } else if (arg1.scope.equals("method")) {
            // TODO
            arg1.addLocal(arg1.currentClass, arg0.f1.f0.tokenImage, visitor.visit(arg0.f0, arg1));
            printWithIndents(arg0.f1.f0.tokenImage + " = 0", arg1);
        } else {
            Scope.dipTfOut();
        }
    }

     /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public void visit(MethodDeclaration arg0, Scope arg1) {
        arg0.f4.accept(this, arg1);
        String methodName = arg0.f2.f0.tokenImage;
        String paramList = String.join(" ", arg1.currentParameterList);
        if (!paramList.equals("")) {
            paramList = " " + paramList;
        }
        printWithIndents("func " + arg1.currentClass + "_" + methodName + "(this" + paramList + ")", arg1);
        arg1.currentParameterList.clear();
        arg1.scope = "method";
        arg1.locals.put(arg1.currentClass, new HashMap<String, String>());
        arg0.f7.accept(this, arg1);
        arg0.f8.accept(this, arg1);
        int retK = arg1.k;
        arg0.f10.accept(this, arg1);
        printWithIndents("return " + createVar(retK), arg1);
        printWithIndents("", arg1);
        arg1.stepOut();
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    @Override
    public void visit(FormalParameterList arg0, Scope arg1) {
        arg1.currentParameterList.clear();
        arg1.inFormalParameterList = true;
        arg0.f0.accept(this, arg1);
        arg0.f1.accept(this, arg1);
        arg1.inFormalParameterList = false;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */    
    @Override
    public void visit(FormalParameter arg0, Scope arg1) {
        TypecheckVisitor visitor = new TypecheckVisitor();
        // printWithIndents("FormalParameter");
        String paramType = arg0.f0.accept(visitor, arg1);
        arg1.addFormalParam(arg1.currentClass, arg0.f1.f0.tokenImage, paramType);

        if (arg1.inFormalParameterList) {
            arg1.currentParameterList.add(arg0.f1.f0.tokenImage);
        }
    }

     /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public void visit(FormalParameterRest arg0, Scope arg1) {
        arg0.f1.accept(this, arg1);
    }

    @Override
    public void visit(Type arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    @Override
    public void visit(ArrayType arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
        arg0.f1.accept(this, arg1);
        arg0.f2.accept(this, arg1);
    }

    @Override
    public void visit(BooleanType arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    @Override
    public void visit(IntegerType arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    @Override
    public void visit(Statement arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    @Override
    public void visit(Block arg0, Scope arg1) {
        arg0.f1.accept(this, arg1);
    }

    /**
     * f0 -> Identifier() f1 -> "=" f2 -> Expression() f3 -> ";"
     */
    @Override
    public void visit(AssignmentStatement arg0, Scope arg1) {
        // is local variable or method param
        if (arg1.locals.get(arg1.currentClass).containsKey(arg0.f0.f0.tokenImage)
                || (arg1.formalParams.containsKey(arg1.currentClass) && arg1.formalParams.get(arg1.currentClass).containsKey(arg0.f0.f0.tokenImage))) {
            int res = arg1.k;
            arg0.f2.accept(this, arg1);
            // Store back into local variable
            printWithIndents(arg0.f0.f0.tokenImage + " = " + createVar(res), arg1);
        } else { // is field
            int res = arg1.k;
            arg0.f2.accept(this, arg1);
            String parent = arg1.currentClass;
            while (!arg1.propertyTable.get(arg1.currentClass).containsKey(parent+"#"+arg0.f0.f0.tokenImage)) {
                parent = arg1.subtypes.get(parent);
            }
            int offset = arg1.propertyTable.get(arg1.currentClass).get(parent+"#"+arg0.f0.f0.tokenImage);
            // Store onto heap if is a field
            // if (res == 94) {
            //     printWithIndents("print(this)", arg1);
            // }
            printWithIndents("[this + " + Integer.toString(offset) + "] = " + createVar(res), arg1);
        }
        arg1.k += 1;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public void visit(ArrayAssignmentStatement arg0, Scope arg1) {
        // is local variable or method param
        if (arg1.locals.get(arg1.currentClass).containsKey(arg0.f0.f0.tokenImage)
                || (arg1.formalParams.containsKey(arg1.currentClass) && arg1.formalParams.get(arg1.currentClass).containsKey(arg0.f0.f0.tokenImage))) {
            int i = arg1.k;
            arg1.k += 4;
            arg0.f2.accept(this, arg1);
            int rhsK = arg1.k;
            arg0.f5.accept(this, arg1);
            printWithIndents(createVar(i+1) + " = 4", arg1); // wk+1 = 4
            printWithIndents(createVar(i+2) + " = 1", arg1); // wk+2 = 1
            printWithIndents(createVar(i+3) + " = " + createVar(i+4) + " + " + createVar(i+2), arg1); // wk+3 = wk+4 + wk+2 (1)
            printWithIndents(createVar(i+2) + " = " + createVar(i+3) + " * " + createVar(i+1), arg1); // wk+2 = wk+3 * wk+1 (4)
            printWithIndents(createVar(i+1) + " = " + arg0.f0.f0.tokenImage + " + " + createVar(i+2), arg1);
            // TODO: bounds check
            // [arg0.f0.f0.tokenImage + (w_indexK + 1) * 4] = w_rhsK
            printWithIndents("[" + createVar(i+1) + " + 0] = " + createVar(rhsK), arg1);
        } else { // is field
            int i = arg1.k;
            arg1.k += 4;
            arg0.f2.accept(this, arg1);
            int rhsK = arg1.k;
            arg0.f5.accept(this, arg1);
            String parent = arg1.currentClass;
            while (!arg1.propertyTable.get(arg1.currentClass).containsKey(parent+"#"+arg0.f0.f0.tokenImage)) {
                parent = arg1.subtypes.get(parent);
            }
            int offset = arg1.propertyTable.get(arg1.currentClass).get(parent+"#"+arg0.f0.f0.tokenImage);
            String indexVar = createVar(i+4);
            printWithIndents(createVar(i+1) + " = 4", arg1); // wk+1 = 4
            printWithIndents(createVar(i+2) + " = 1", arg1); // wk+2 = 1
            printWithIndents(createVar(i+3) + " = " + indexVar + " + " + createVar(i+2), arg1); // wk+3 = wk+4 + wk+2 (1)
            printWithIndents(createVar(i+2) + " = " + createVar(i+3) + " * " + createVar(i+1), arg1); // wk+2 = wk+3 * wk+1 (4)
            printWithIndents(createVar(i) + " = " + "[this + " + Integer.toString(offset) + "]", arg1); // wk = [this + offset]
            printWithIndents(createVar(i+1) + " = " + createVar(i) + " + " + createVar(i+2), arg1); // wk+1 = wk + wk+2
            // TODO: bounds check
            // [w_k + w_k+4] = w_rhsK
            printWithIndents("[" + createVar(i+1) + " + 0] = " + createVar(rhsK), arg1);
        }
        arg1.k += 1;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public void visit(IfStatement arg0, Scope arg1) {
        int expK = arg1.k;
        String elseLabel = "else_" + Integer.toString(expK);
        String endLabel = "end_" + Integer.toString(expK);
        arg0.f2.accept(this, arg1);
        printWithIndents("if0 " + createVar(expK) + " goto " + elseLabel, arg1);
        arg0.f4.accept(this, arg1);
        printWithIndents("goto " + endLabel, arg1);
        printLabel(elseLabel);
        arg0.f6.accept(this, arg1);
        printLabel(endLabel);
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public void visit(WhileStatement arg0, Scope arg1) {
        int k = arg1.k;
        String loopLabel = "loop_" + Integer.toString(k);
        String endLabel = "endLoop_" + Integer.toString(k);
        printLabel(loopLabel);
        arg0.f2.accept(this, arg1);
        printWithIndents("if0 " + createVar(k) + " goto " + endLabel, arg1);
        arg0.f4.accept(this, arg1);
        printWithIndents("goto " + loopLabel, arg1);
        printLabel(endLabel);
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public void visit(PrintStatement arg0, Scope arg1) {
        int k = arg1.k;
        arg0.f2.accept(this, arg1);
        printWithIndents("print(" + createVar(k) + ")", arg1);
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    @Override
    public void visit(Expression arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    @Override
    public void visit(AndExpression arg0, Scope arg1) {
        int retK = arg1.k;
        arg1.k += 1;
        int lhsK = arg1.k;
        arg0.f0.accept(this, arg1);
        int rhsK = arg1.k;
        arg0.f2.accept(this, arg1);
        printWithIndents(createVar(retK) + " = " + createVar(lhsK) + " * " + createVar(rhsK), arg1);
        arg1.k += 1;
    }

    @Override
    public void visit(CompareExpression arg0, Scope arg1) {
        int res = arg1.k;
        arg1.k += 1;
        int f0 = arg1.k;
        arg0.f0.accept(this, arg1); // value of f0 -> Primary() in wk+1, return K
        arg1.k += 1;
        int f2 = arg1.k;
        arg0.f2.accept(this, arg1); // value of f2 -> Primary() in wK+1, return _
        printWithIndents(createVar(res) + " = " + createVar(f0) + " < " + createVar(f2), arg1); // wk = f0 + f2
        arg1.k += 1;
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "+" f2 -> PrimaryExpression()
     */
    @Override
    public void visit(PlusExpression arg0, Scope arg1) {
        int res = arg1.k;
        arg1.k += 1;
        int f0 = arg1.k;
        arg0.f0.accept(this, arg1); // value of f0 -> Primary() in wk+1, return K
        arg1.k += 1;
        int f2 = arg1.k;
        arg0.f2.accept(this, arg1); // value of f2 -> Primary() in wK+1, return _
        printWithIndents(createVar(res) + " = " + createVar(f0) + " + " + createVar(f2), arg1); // wk = f0 + f2
        arg1.k += 1;
    }

    @Override
    public void visit(MinusExpression arg0, Scope arg1) {
        int res = arg1.k;
        arg1.k += 1;
        int f0 = arg1.k;
        arg0.f0.accept(this, arg1); // value of f0 -> Primary() in wk+1, return K
        arg1.k += 1;
        int f2 = arg1.k;
        arg0.f2.accept(this, arg1); // value of f2 -> Primary() in wK+1, return _
        printWithIndents(createVar(res) + " = " + createVar(f0) + " - " + createVar(f2), arg1); // wk = f0 + f2
        arg1.k += 1;
    }

    @Override
    public void visit(TimesExpression arg0, Scope arg1) {
        int res = arg1.k;
        arg1.k += 1;
        int f0 = arg1.k;
        arg0.f0.accept(this, arg1); // value of f0 -> Primary() in wk+1, return K
        arg1.k += 1;
        int f2 = arg1.k;
        arg0.f2.accept(this, arg1); // value of f2 -> Primary() in wK+1, return _
        printWithIndents(createVar(res) + " = " + createVar(f0) + " * " + createVar(f2), arg1); // wk = f0 + f2
        arg1.k += 1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public void visit(ArrayLookup arg0, Scope arg1) {
        int res = arg1.k;

        arg1.k += 4;
        int heapK = arg1.k;
        arg0.f0.accept(this, arg1);
        int indexK = arg1.k;
        arg0.f2.accept(this, arg1);
        // Out of bounds checking
        String outOfBoundsLabel = "outOfBounds_" + Integer.toString(res);
        String passLabel = "pass_" + Integer.toString(res);
        printWithIndents(createVar(res+1) + " = [" + createVar(heapK) + " + 0]", arg1); // res+1 = [heapK + 0]
        printWithIndents(createVar(res+2) + " = " + createVar(indexK) + " < " + createVar(res+1), arg1); // res+2 = indexK < res+1
        printWithIndents("if0 " + createVar(res+2) + " goto " + outOfBoundsLabel, arg1); // if0 res+2 goto outOfBoundsRes
        printWithIndents(createVar(res+1) + " = 4", arg1);
        printWithIndents(createVar(res+2) + " = 1", arg1);
        printWithIndents(createVar(res+3) + " = " + createVar(indexK) + " + " + createVar(res+2), arg1);
        printWithIndents(createVar(res+2) + " = " + createVar(res+3) + " * " + createVar(res+1), arg1);
        printWithIndents(createVar(res+1) + " = " + createVar(heapK) + " + " + createVar(res+2), arg1);
        printWithIndents(createVar(res) + " = " + "[" + createVar(res+1) + " + 0]", arg1);
        printWithIndents("goto " + passLabel, arg1);
        printLabel(outOfBoundsLabel); // outOfBoundsRes:
        printWithIndents("error(\"array index out of bounds\")", arg1);// error("array index out of bounds")
        printLabel(passLabel);
        arg1.k += 1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public void visit(ArrayLength arg0, Scope arg1) {
        int res = arg1.k;

        arg1.k += 1;
        int heapK = arg1.k;
        arg0.f0.accept(this, arg1);
        printWithIndents(createVar(res) + " = " + "[" + createVar(heapK) + " + 0]", arg1);
        arg1.k += 1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public void visit(MessageSend arg0, Scope arg1) {
        TypecheckVisitor visitor = new TypecheckVisitor();
        // printWithIndents("MessageSend");
        String callerType = arg0.f0.accept(visitor, arg1);
        System.out.println(callerType);
        int res = arg1.k;
        arg1.k += 2;
        int primaryK = arg1.k; // heap address of caller stored wprimaryK
        arg0.f0.accept(this, arg1);
        String methodName = arg0.f2.f0.tokenImage;
        String originatingClass = arg1.methodTable.get(callerType).get(methodName).get(0);
        arg0.f4.accept(this, arg1);
        // load method
        printWithIndents(createVar(res+1) + " = @" + originatingClass + "_" + methodName, arg1);
        String expList = String.join(" ", arg1.currentExpressionList);
        if (!expList.equals("")) {
            expList = " " + expList;
        }
        printWithIndents(createVar(res) + " = " + "call " + createVar(res+1) + "(" + createVar(primaryK) + expList + ")", arg1);
        arg1.currentExpressionList.clear();
        arg1.k += 1;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    @Override
    public void visit(ExpressionList arg0, Scope arg1) {
        arg1.currentExpressionList.clear();
        arg1.inExpressionList = true;
        int initialExpressionK = arg1.k;
        arg0.f0.accept(this, arg1);
        arg1.currentExpressionList.add(createVar(initialExpressionK));
        arg0.f1.accept(this, arg1);
        arg1.inExpressionList = false;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public void visit(ExpressionRest arg0, Scope arg1) {
        int k = arg1.k;
        arg0.f1.accept(this, arg1);
        if (arg1.inExpressionList) {
            arg1.currentExpressionList.add(createVar(k));
        }
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    @Override
    public void visit(PrimaryExpression arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
    }

    @Override
    public void visit(IntegerLiteral arg0, Scope arg1) {
        printWithIndents(createVar(arg1.k) + " = " + arg0.f0.tokenImage, arg1);
        arg1.k += 1;
    }

    @Override
    public void visit(TrueLiteral arg0, Scope arg1) {
        printWithIndents(createVar(arg1.k) + " = 1", arg1);
        arg1.k += 1;
    }

    @Override
    public void visit(FalseLiteral arg0, Scope arg1) {
        printWithIndents(createVar(arg1.k) + " = 0", arg1);
        arg1.k += 1;
    }

    @Override
    public void visit(Identifier arg0, Scope arg1) {
        // String type = arg1.getTypeOfIdentifier(arg1.currentClass,
        // arg0.f0.tokenImage);
        if (arg1.locals.get(arg1.currentClass).containsKey(arg0.f0.tokenImage)
                || (arg1.formalParams.containsKey(arg1.currentClass) && arg1.formalParams.get(arg1.currentClass).containsKey(arg0.f0.tokenImage))) { // is local variable or
                                                                                               // param within method
            printWithIndents(createVar(arg1.k) + " = " + arg0.f0.tokenImage, arg1);
        } else {
            String parent = arg1.currentClass;
            while (!arg1.propertyTable.get(arg1.currentClass).containsKey(parent+"#"+arg0.f0.tokenImage)) {
                parent = arg1.subtypes.get(parent);
            }
            int offset = arg1.propertyTable.get(arg1.currentClass).get(parent+"#"+arg0.f0.tokenImage);
            printWithIndents(createVar(arg1.k) + " = " + "[this + " + Integer.toString(offset) + "]", arg1);
        }
        arg1.k += 1;
    }

    /**
     * f0 -> "this"
     */
    @Override
    public void visit(ThisExpression arg0, Scope arg1) {
        printWithIndents(createVar(arg1.k) + " = this", arg1);
        arg1.k += 1;
    }

    /**
     * f0 -> "new" f1 -> "int" f2 -> "[" f3 -> Expression() f4 -> "]"
     */
    @Override
    public void visit(ArrayAllocationExpression arg0, Scope arg1) {
        int res = arg1.k;
        arg1.k += 5;
        arg0.f3.accept(this, arg1); // value of f3 -> Expression() in wk+5
        ArrayList<String> vars = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            vars.add(createVar(res + i));
        }
        printWithIndents(vars.get(3) + " = 4", arg1); // wk+3 = 4
        printWithIndents(vars.get(4) + " = 1", arg1); // wk+4 = 1
        printWithIndents(vars.get(1) + " = " + vars.get(5) + " + " + vars.get(4), arg1); // wk+1 = wk+5 + wk+4 (1)
        printWithIndents(vars.get(2) + " = " + vars.get(1) + " * " + vars.get(3), arg1); // wk+2 = wk+1 * wk+3 (4)
        printWithIndents(vars.get(0) + " = " + "alloc(" + vars.get(2) + ")", arg1); // wk = alloc(wk+2)

        // Set length of array
        printWithIndents("[" + vars.get(0) + " + 0] = " + vars.get(5), arg1); // [wk] = wk+5

        // Loop through and initialize array values to 0
        // Nevermind Sparrow auto initializes to 0
        // printWithIndents("if0 " + createVar(res+2) + " goto loop" +
        // Integer.toString(res+3)); // if0 wk+4 goto loopwk+3
        // printWithIndents("[" + createVar(res) + " + " + createVar(res+2) + "] =
        // 0"); // [wk + wk+2] = 0
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public void visit(AllocationExpression arg0, Scope arg1) {
        // TypecheckVisitor visitor = new TypecheckVisitor();
        // printWithIndents("AllocationExpression");
        String type = arg0.f1.f0.tokenImage;
        int res = arg1.k;
        int largestSize = 4; // In the case of no fields, still leave 4 to store method table
        for (int offset : arg1.propertyTable.get(type).values()) {
            largestSize = Math.max(offset+4, largestSize);
        }
        // property table address in res
        printWithIndents(createVar(res+2) + " = " + Integer.toString(largestSize), arg1);
        printWithIndents(createVar(res) + " = alloc(" + createVar(res+2) + ")", arg1);
        largestSize = 0;
        for (ArrayList<String> methodData : arg1.methodTable.get(type).values()) {
            int offset = Integer.valueOf(methodData.get(1));
            largestSize = Math.max(offset+4, largestSize);
        }
        // method table heap address in res+1
        printWithIndents(createVar(res+2) + " = " + Integer.toString(largestSize), arg1);
        printWithIndents(createVar(res+1) + " = alloc(" + createVar(res+2) + ")", arg1);
        printWithIndents("[" + createVar(res) + " + 0] = " + createVar(res+1), arg1); // [propertyTableAddress + 0] = methodTableAdress
        
        // populate method table
        for (String methodName : arg1.methodTable.get(type).keySet()) {
            String originatingClass = arg1.methodTable.get(type).get(methodName).get(0);
            String offset = arg1.methodTable.get(type).get(methodName).get(1);
            printWithIndents(createVar(res+2) + " = @" + originatingClass + "_" + methodName , arg1);
            printWithIndents("[" + createVar(res+1) + " + " + offset + "] = " + createVar(res+2), arg1);
        }

        arg1.k += 3;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    @Override
    public void visit(NotExpression arg0, Scope arg1) {
        int res = arg1.k;
        arg1.k += 2;
        int expressionK = arg1.k;
        arg0.f1.accept(this, arg1);
        printWithIndents(createVar(res+1) + " = 1", arg1);
        printWithIndents(createVar(res) + " = " + createVar(expressionK) + " < " + createVar(res+1), arg1);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public void visit(BracketExpression arg0, Scope arg1) {
        arg0.f1.accept(this, arg1);
    }

}

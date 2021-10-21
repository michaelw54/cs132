import cs132.minijava.visitor.*;
import cs132.minijava.syntaxtree.*;
import java.util.Enumeration;

public class TypecheckVisitor implements GJVisitor<String, Scope> {
    
    private boolean isArray(String arrayType) {
        return arrayType.substring(Math.max(arrayType.length() - 2, 0)).equals("[]");
    }

    private String getArrayType(String arrayType) {
        return arrayType.substring(0, arrayType.length() - 2);
    }

    @Override
    public String visit(NodeList arg0, Scope arg1) {
        Enumeration<Node> elements = arg0.elements();
        while (elements.hasMoreElements()) {
            elements.nextElement().accept(this, arg1);
        }
        return "";
    }

    @Override
    public String visit(NodeListOptional arg0, Scope arg1) {
        String currList = "";
        Enumeration<Node> elements = arg0.elements();
        if (arg0.present()) {
            while (elements.hasMoreElements()) {
                String elementType = elements.nextElement().accept(this, arg1);
                if (currList.length() > 0) {
                    currList = currList + "," + elementType;
                } else {
                    currList = elementType;
                }
            }
        }
        return currList;
    }

    @Override
    public String visit(NodeOptional arg0, Scope arg1) {
        if (arg0.present()) {
            return arg0.node.accept(this, arg1);
        }
        return "";
    }

    @Override
    public String visit(NodeSequence arg0, Scope arg1) {
        Enumeration<Node> elements = arg0.elements();
        while (elements.hasMoreElements()) {
            elements.nextElement().accept(this, arg1);
        }
        return "";
    }

    @Override
    public String visit(NodeToken arg0, Scope arg1) {
        return "";
    }

    @Override
    public String visit(Goal arg0, Scope arg1) {
        arg0.f0.accept(this, arg1);
        arg0.f1.accept(this, arg1);
        return "void";
    }

    @Override
    public String visit(MainClass arg0, Scope arg1) {
        arg0.f14.accept(this, arg1);
        arg0.f15.accept(this, arg1);
        return "void";
    }

    @Override
    public String visit(TypeDeclaration arg0, Scope arg1) {
        return arg0.f0.accept(this, arg1);
    }

    @Override
    public String visit(ClassDeclaration arg0, Scope arg1) {
        arg1.scope = "class";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        arg0.f3.accept(this, arg1);
        arg0.f4.accept(this, arg1);
        // System.out.println("Class: " + arg1.currentClass);
        arg1.stepOut();
        return "void";
    }

    @Override
    public String visit(ClassExtendsDeclaration arg0, Scope arg1) {
        arg1.scope = "class";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        if (!arg1.classes.contains(arg0.f3.f0.tokenImage)) {
            // Can't extend a class that doesn't exist
            Scope.dipTfOut();
        }
        arg0.f5.accept(this, arg1);
        arg0.f6.accept(this, arg1);
        // System.out.println("Class: " + arg1.currentClass);
        arg1.stepOut();
        return "void";
    }

    @Override
    public String visit(VarDeclaration arg0, Scope arg1) {
        if (arg1.scope.equals("class")) {
            if (arg1.fields.containsKey(arg0.f1.f0.tokenImage)) {
                return Scope.dipTfOut();
            }
            arg1.addField(arg1.currentClass, arg0.f1.f0.tokenImage, arg0.f0.accept(this, arg1));
        } else if (arg1.scope.equals("method")) {
            if (arg1.locals.containsKey(arg0.f1.f0.tokenImage) || arg1.formalParams.containsKey(arg0.f1.f0.tokenImage)) {
                return Scope.dipTfOut();
            }
            arg1.addLocal(arg1.currentClass, arg0.f1.f0.tokenImage, arg0.f0.accept(this, arg1));
        } else {
            return Scope.dipTfOut();
        }
        return "void";
    }

    @Override
    public String visit(MethodDeclaration arg0, Scope arg1) {
        arg1.scope = "method";
        String returnType = arg0.f1.accept(this, arg1);
        // System.out.println("type pass: " + arg1.currentClass);
        arg0.f4.accept(this, arg1);
        // System.out.println("formal params pass: " + arg1.currentClass);
        arg0.f7.accept(this, arg1);
        // System.out.println("vars pass: " + arg1.currentClass);
        arg0.f8.accept(this, arg1);
        // System.out.println("statements pass: " + arg1.currentClass);
        String finalReturnType = arg0.f10.accept(this, arg1);
        if (finalReturnType.equals(returnType)) {
            arg1.stepOut();
            return "void";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(FormalParameterList arg0, Scope arg1) {
        String paramType = arg0.f0.accept(this, arg1);
        String restType = arg0.f1.accept(this, arg1);
        return paramType + "," + restType;
    }

    @Override
    public String visit(FormalParameter arg0, Scope arg1) {
        if (arg1.formalParams.containsKey(arg0.f1.f0.tokenImage)) {
            return Scope.dipTfOut();
        }
        String paramType = arg0.f0.accept(this, arg1);
        arg1.addFormalParam(arg1.currentClass, arg0.f1.f0.tokenImage, paramType);
        return paramType;
    }

    @Override
    public String visit(FormalParameterRest arg0, Scope arg1) {
        return arg0.f1.accept(this, arg1);
    }

    @Override
    public String visit(Type arg0, Scope arg1) {
        arg1.enteringPotentialUserDefinedType = true;
        String result = arg0.f0.accept(this, arg1);
        arg1.enteringPotentialUserDefinedType = false;
        return result;
    }

    @Override
    public String visit(ArrayType arg0, Scope arg1) {
        // In the case of MiniJava, we only have int[]
        // Do this for flexibility as we accounted for different type arrays in other visit methods
        return arg0.f0.tokenImage + "[]";
    }

    @Override
    public String visit(BooleanType arg0, Scope arg1) {
        return "boolean";
    }

    @Override
    public String visit(IntegerType arg0, Scope arg1) {
        return "int";
    }

    @Override
    public String visit(Statement arg0, Scope arg1) {
        return arg0.f0.accept(this, arg1);
    }

    @Override
    public String visit(Block arg0, Scope arg1) {
        return arg0.f1.accept(this, arg1);
    }

    @Override
    public String visit(AssignmentStatement arg0, Scope arg1) {
        String identifierType = arg0.f0.accept(this, arg1);
        String expressionType = arg0.f2.accept(this, arg1);
        if (arg1.isSubtype(expressionType, identifierType)) {
            return "void";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(ArrayAssignmentStatement arg0, Scope arg1) {
        String identifierType = arg0.f0.accept(this, arg1);
        if (!isArray(identifierType) || !arg0.f2.accept(this, arg1).equals("int")) {
            return Scope.dipTfOut();
        }
        String arrayType = getArrayType(identifierType);
        if (arg0.f5.accept(this, arg1).equals(arrayType)) {
            return "void";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(IfStatement arg0, Scope arg1) {
        if (!arg0.f2.accept(this, arg1).equals("boolean")) {
            return Scope.dipTfOut();
        }
        arg0.f4.accept(this, arg1);
        arg0.f6.accept(this, arg1);
        return "void";
    }

    @Override
    public String visit(WhileStatement arg0, Scope arg1) {
        if (!arg0.f2.accept(this, arg1).equals("boolean")) {
            return Scope.dipTfOut();
        }
        arg0.f4.accept(this, arg1);
        return "void";
    }

    @Override
    public String visit(PrintStatement arg0, Scope arg1) {
        String expType = arg0.f2.accept(this, arg1);
        if (expType.equals("int") || expType.equals("string")) {
            return "void";
        }
        // Can't print this type
        return Scope.dipTfOut();
    }

    @Override
    public String visit(Expression arg0, Scope arg1) {
        return arg0.f0.accept(this, arg1);
    }

    @Override
    public String visit(AndExpression arg0, Scope arg1) {
        if (arg0.f0.accept(this, arg1).equals("boolean") && arg0.f2.accept(this, arg1).equals("boolean")) {
            return "boolean";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(CompareExpression arg0, Scope arg1) {
        if (arg0.f0.accept(this, arg1).equals("int") && arg0.f2.accept(this, arg1).equals("int")) {
            return "boolean";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(PlusExpression arg0, Scope arg1) {
        if (arg0.f0.accept(this, arg1).equals("int") && arg0.f2.accept(this, arg1).equals("int")) {
            return "int";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(MinusExpression arg0, Scope arg1) {
        if (arg0.f0.accept(this, arg1).equals("int") && arg0.f2.accept(this, arg1).equals("int")) {
            return "int";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(TimesExpression arg0, Scope arg1) {
        if (arg0.f0.accept(this, arg1).equals("int") && arg0.f2.accept(this, arg1).equals("int")) {
            return "int";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(ArrayLookup arg0, Scope arg1) {
        String arrayType = arg0.f0.accept(this, arg1);
        // Check if identifier is an array by checking if is in format "Type[]"
        if (!isArray(arrayType)) {
            return Scope.dipTfOut();
        }
        arrayType = getArrayType(arrayType);
        // check if array index is integer. If so, return the "Type" part of "Type[]"
        if (arg0.f2.accept(this, arg1).equals("int")) {
            return arrayType;
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(ArrayLength arg0, Scope arg1) {
        String type = arg0.f0.accept(this, arg1);
        // Check if type is in format "Type[]"
        if (isArray(type)) {
            return "int";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(MessageSend arg0, Scope arg1) {
        String instanceType = arg0.f0.accept(this, arg1);
        String methodName = arg0.f2.f0.tokenImage;
        String argTypes = arg0.f4.accept(this, arg1);

        String returnType = arg1.getMethodReturnType(instanceType, methodName);
        String paramTypes = arg1.getMethodParamTypes(instanceType, methodName);
        String[] argTypesList = argTypes.split(",");
        String [] paramTypesList = paramTypes.split(",");
        if (argTypesList.length != paramTypesList.length) {
            return Scope.dipTfOut();
        }
        for (int i = 0; i < argTypesList.length; i++) {
            if (!arg1.isSubtype(argTypesList[i], paramTypesList[i])) {
                return Scope.dipTfOut();
            }
        }
        return returnType;
    }

    @Override
    public String visit(ExpressionList arg0, Scope arg1) {
        return arg0.f0.accept(this, arg1) + "," + arg0.f1.accept(this, arg1);
    }

    @Override
    public String visit(ExpressionRest arg0, Scope arg1) {
        return arg0.f1.accept(this, arg1);
    }

    @Override
    public String visit(PrimaryExpression arg0, Scope arg1) {
        return arg0.f0.accept(this, arg1);
    }

    @Override
    public String visit(IntegerLiteral arg0, Scope arg1) {
        return "int";
    }

    @Override
    public String visit(TrueLiteral arg0, Scope arg1) {
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral arg0, Scope arg1) {
        return "boolean";
    }

    @Override
    public String visit(Identifier arg0, Scope arg1) {
        return arg1.getTypeOfIdentifier(arg1.currentClass, arg0.f0.tokenImage);
    }

    @Override
    public String visit(ThisExpression arg0, Scope arg1) {
        if (arg1.scope.equals("class") || arg1.scope.equals("method")) {
            return arg1.currentClass;
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(ArrayAllocationExpression arg0, Scope arg1) {
        if (arg0.f3.accept(this, arg1).equals("int")) {
            return "int[]";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(AllocationExpression arg0, Scope arg1) {
        // System.out.println(arg1.classes.toString());
        if (arg1.classes.contains(arg0.f1.f0.tokenImage)) {
            return arg0.f1.f0.tokenImage;
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(NotExpression arg0, Scope arg1) {
        if (arg0.f1.accept(this, arg1).equals("boolean")) {
            return "boolean";
        }
        return Scope.dipTfOut();
    }

    @Override
    public String visit(BracketExpression arg0, Scope arg1) {
        return arg0.f1.accept(this, arg1);
    }

}

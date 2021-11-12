import cs132.minijava.visitor.*;
import cs132.minijava.syntaxtree.*;
import java.util.Enumeration;
import java.util.HashSet;

public class ClassVisitor extends GJDepthFirst<Boolean, Scope> {

    @Override
    public Boolean visit(NodeList arg0, Scope arg1) {
        Enumeration<Node> elements = arg0.elements();
        while (elements.hasMoreElements()) {
            if (!elements.nextElement().accept(this, arg1)) {
                Scope.dipTfOut();
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(NodeListOptional arg0, Scope arg1) {
        Enumeration<Node> elements = arg0.elements();
        if (arg0.present()) {
            while (elements.hasMoreElements()) {
                if (!elements.nextElement().accept(this, arg1)) {
                    Scope.dipTfOut();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Boolean visit(NodeOptional arg0, Scope arg1) {
        if (arg0.present()) {
            return arg0.node.accept(this, arg1);
        }
        return true;
    }

    @Override
    public Boolean visit(NodeSequence arg0, Scope arg1) {
        Enumeration<Node> elements = arg0.elements();
        while (elements.hasMoreElements()) {
            if (!elements.nextElement().accept(this, arg1)) {
                Scope.dipTfOut();
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(NodeToken arg0, Scope arg1) {
        return true;
    }

    @Override
    public Boolean visit(Goal arg0, Scope arg1) {
        Boolean main = arg0.f0.accept(this, arg1);
        Boolean rest = arg0.f1.accept(this, arg1);
        arg1.populateOffsets();
        // System.out.println(arg1.propertyTable.toString());
        // System.out.println();
        System.out.println(arg1.methodTable.toString());
        System.out.println();
        // System.out.println(arg1.fields.toString());
        return main && rest;
    }

    @Override
    public Boolean visit(MainClass arg0, Scope arg1) {
        arg1.scope = "main";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        // Duplicate class definition
        if (arg1.classes.contains(arg0.f1.f0.tokenImage)) {
            Scope.dipTfOut();
            return false;
        }
        // So apparently we can create instances of the main class
        arg1.classes.add(arg0.f1.f0.tokenImage);
        return true;
    }

    @Override
    public Boolean visit(TypeDeclaration arg0, Scope arg1) {
        return arg0.f0.accept(this, arg1);
    }

    @Override
    public Boolean visit(ClassDeclaration arg0, Scope arg1) {
        arg1.scope = "class";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        // Duplicate class definition
        if (arg1.classes.contains(arg0.f1.f0.tokenImage)) {
            Scope.dipTfOut();
            return false;
        }
        arg1.classes.add(arg0.f1.f0.tokenImage);
        // Variables processed in second pass (TypecheckVisitor)
        arg0.f3.accept(this, arg1);
        arg0.f4.accept(this, arg1);
        arg1.stepOut();
        return true;
    }

    private Boolean checkCyclicExtensions(String className, Scope scope) {
        HashSet<String> visited = new HashSet<>();
        while (className != null) {
            if (visited.contains(className)) {
                return true; // cycle found
            }
            visited.add(className);
            // set to parent class
            className = scope.subtypes.get(className);
        }
        return false; // cycle not found
    }

    @Override
    public Boolean visit(ClassExtendsDeclaration arg0, Scope arg1) {
        arg1.scope = "class";
        arg1.currentClass = arg0.f1.f0.tokenImage;
        // Duplicate class definition
        if (arg1.classes.contains(arg0.f1.f0.tokenImage)) {
            Scope.dipTfOut();
            return false;
        }
        arg1.classes.add(arg0.f1.f0.tokenImage);
        arg1.subtypes.put(arg1.currentClass, arg0.f3.f0.tokenImage);
        if (checkCyclicExtensions(arg1.currentClass, arg1)) {
            Scope.dipTfOut();
            return false;
        }
        arg0.f6.accept(this, arg1);
        arg1.stepOut();
        return true;
    }

    @Override
    public Boolean visit(MethodDeclaration arg0, Scope arg1) {
        arg1.scope = "method";
        String methodName = arg0.f2.f0.tokenImage;
        TypecheckVisitor visitor = new TypecheckVisitor();
        arg1.addMethod(arg1.currentClass, methodName, visitor.visit(arg0.f1, arg1));
        arg1.addMethodParams(arg1.currentClass, methodName, visitor.visit(arg0.f4, arg1));
        arg1.stepOut();
        return true;
    }

    @Override
    public Boolean visit(VarDeclaration arg0, Scope arg1) {
        TypecheckVisitor visitor = new TypecheckVisitor();
        if (arg1.scope.equals("class")) {
            if (arg1.fields.containsKey(arg0.f1.f0.tokenImage)) {
                return false;
            }
            arg1.addField(arg1.currentClass, arg0.f1.f0.tokenImage, visitor.visit(arg0.f0, arg1));
        } else if (arg1.scope.equals("method")) {
            if (arg1.locals.containsKey(arg0.f1.f0.tokenImage) || arg1.formalParams.containsKey(arg0.f1.f0.tokenImage)) {
                return false;
            }
            arg1.addLocal(arg1.currentClass, arg0.f1.f0.tokenImage, visitor.visit(arg0.f0, arg1));
        } else {
            return false;
        }
        return true;
    }
}

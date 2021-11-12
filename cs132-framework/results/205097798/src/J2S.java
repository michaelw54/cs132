import cs132.minijava.MiniJavaParser;
import cs132.minijava.syntaxtree.*;
import java.util.HashMap;

public class J2S {
    public static void main(String[] args) {
        try {
            Goal root = new MiniJavaParser(System.in).Goal();
            Scope scope = new Scope("main", "main");
            root.accept(new ClassVisitor(), scope);
            root.accept(new MainVisitor(), scope);
            // if (checkForOverloadedMethods(scope)) {
            //     throw new Exception();
            // }
            // root.accept(new TypecheckVisitor(), scope);
            // System.out.println("Program type checked successfully");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private static Boolean checkForOverloadedMethods(Scope scope) {
        for (String className : scope.classes) {
            HashMap<String, String> methodReturnTypes = scope.methods.get(className);
            HashMap<String, String> methodParamTypes = scope.methodParams.get(className);
            String currClass = scope.subtypes.get(className);
            while (currClass != null) {
                for (String methodName : methodReturnTypes.keySet()) {
                    HashMap<String, String> parentMethodReturnTypes = scope.methods.get(currClass);
                    HashMap<String, String> parentMethodParamTypes = scope.methodParams.get(currClass);
                    if (parentMethodReturnTypes.containsKey(methodName)) {
                        if (!parentMethodReturnTypes.get(methodName).equals(methodReturnTypes.get(methodName)) ||
                            !parentMethodParamTypes.get(methodName).equals(methodParamTypes.get(methodName))
                        ) {
                            return true;
                        }
                    }
                }
                currClass = scope.subtypes.get(currClass);
            }
        }
        return false; // No overloaded methods, pass!
    }
}
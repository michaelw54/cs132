import java.util.HashMap;
import java.util.HashSet;

public class Scope {
    public String scope; // main, class, method
    public String currentClass;
    public HashSet<String> classes = new HashSet<>();
    public HashMap<String, HashMap<String, String>> fields = new HashMap<>();
    public HashMap<String, HashMap<String, String>> formalParams = new HashMap<>();
    public HashMap<String, HashMap<String, String>> locals = new HashMap<>();

    // [className -> [methodName -> returnType]]
    public HashMap<String, HashMap<String, String>> methods = new HashMap<>();
    // [className -> [methodName -> comma-delimited param types]]
    public HashMap<String, HashMap<String, String>> methodParams = new HashMap<>();
    // [A -> B (A extends B)]
    public HashMap<String, String> subtypes = new HashMap<>();
    public boolean enteringPotentialUserDefinedType = false;

    public static String dipTfOut() {
        System.out.println("Type error");
        System.exit(0);
        return "";
    }

    public Scope(String s, String n) {
        scope = s;
        currentClass = n;
    }

    public boolean isSubtype(String class1, String class2) {
        if (class1.equals(class2)) {
            return true;
        }
        while (subtypes.containsKey(class1)) {
            class1 = subtypes.get(class1);
            if (class1.equals(class2)) {
                return true;
            }
        }
        return false;
    }

    public void addMethod(String className, String identifier, String type) {
        if (methods.containsKey(className)) {
            if (methods.get(className).containsKey(identifier)) {
                // Duplicate method in a class
                dipTfOut();
            }
            methods.get(className).put(identifier, type);
        } else {
            HashMap<String, String> nested = new HashMap<>();
            nested.put(identifier, type);
            methods.put(className, nested);
        }
    }

    public void addMethodParams(String className, String identifier, String paramTypes) {
        if (methodParams.containsKey(className)) {
            if (methodParams.get(className).containsKey(identifier)) {
                // Duplicate method in a class
                dipTfOut();
            }
            methodParams.get(className).put(identifier, paramTypes);
        } else {
            HashMap<String, String> nested = new HashMap<>();
            nested.put(identifier, paramTypes);
            methodParams.put(className, nested);
        }
    }

    public String getMethodReturnType(String className, String identifier) {
        while (className != null) {
            if (methods.containsKey(className) && methods.get(className).containsKey(identifier)) {
                return methods.get(className).get(identifier);
            }
            // Go up to parent class and look for method
            className = subtypes.get(className);
        }
        // Couldn't look up a method, you're fucked
        return dipTfOut();
    }

    public String getMethodParamTypes(String className, String identifier) {
        while (className != null) {
            if (methodParams.containsKey(className) && methodParams.get(className).containsKey(identifier)) {
                return methodParams.get(className).get(identifier);
            }
            // Go up to parent class and look for method
            className = subtypes.get(className);
        }
        // Couldn't look up a method, you're fucked
        return dipTfOut();
    }

    public void addField(String className, String identifier, String type) {
        if (fields.containsKey(className)) {
            if (fields.get(className).containsKey(identifier)) {
                dipTfOut();
            }
            fields.get(className).put(identifier, type);
        } else {
            HashMap<String, String> classFields = new HashMap<>();
            classFields.put(identifier, type);
            fields.put(className, classFields);
        }
    }

    public void addFormalParam(String className, String identifier, String type) {
        if (formalParams.containsKey(className)) {
            if (formalParams.get(className).containsKey(identifier)) {
                dipTfOut();
            }
            formalParams.get(className).put(identifier, type);
        } else {
            HashMap<String, String> methodFormalParams = new HashMap<>();
            methodFormalParams.put(identifier, type);
            formalParams.put(className, methodFormalParams);
        }
    }

    public void addLocal(String className, String identifier, String type) {
        if (locals.containsKey(className)) {
            if (locals.get(className).containsKey(identifier) || 
                (formalParams.containsKey(className) && formalParams.get(className).containsKey(identifier))) {
                dipTfOut();
            }
            locals.get(className).put(identifier, type);
        } else {
            HashMap<String, String> methodLocals = new HashMap<>();
            methodLocals.put(identifier, type);
            locals.put(className, methodLocals);
        }
    }

    public String getTypeOfIdentifier(String className, String identifier) {
        if (enteringPotentialUserDefinedType) {
            return identifier;
        }
        // Search locals, then formal params, then fields in that order
        if (locals.containsKey(className) && locals.get(className).containsKey(identifier)) {
            return locals.get(className).get(identifier);
        } else if (formalParams.containsKey(className) && formalParams.get(className).containsKey(identifier)) {
            return formalParams.get(className).get(identifier);
        } else if (fields.containsKey(className) && fields.get(className).containsKey(identifier)) {
            return fields.get(className).get(identifier);
        }

        // Search parent types for fields
        while (className != null) {
            // System.out.println(fields.toString());
            if (fields.containsKey(className) && fields.get(className).containsKey(identifier)) {
                return fields.get(className).get(identifier);
            }
            className = subtypes.get(className);
        }

        // identifier referenced before declaration
        return dipTfOut();
    }

    public void stepOut() {
        if (scope.equals("method")) {
            formalParams.clear();
            locals.clear();
            scope = "class";
        } else if (scope.equals("class")) {
            scope = "main";
            currentClass = "";
        } else {
            dipTfOut();
        }
    }
}

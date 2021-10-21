import java.util.Scanner;

public class Parser {
    private Scanner sc;
    private String token;
    public Parser() {
        sc = new Scanner(System.in);
        token = "";
    }

    public void parse() {
        goal();
        System.out.println("Program parsed successfully");
    }

    public void nextToken() {
        if (!token.equals("")) {
            debug();
        }
        if (sc.hasNext()) {
            token = sc.next();
        }
    }

    private void debug() {
        System.out.println("Parse error");
        System.exit(1);
    }

    private void eat(String t) {
        if (token.length() >= t.length() && token.substring(0, t.length()).equals(t)) {
            token = token.substring(t.length());
            if (token.isEmpty()) {
                nextToken();
            }
        } else {
            debug();
        }
    }

    private void goal() {
        if (sc.hasNext()) {
            nextToken();
        } else {
            // Empty program is not part of the grammar
            debug();
        }
        S();
        if (sc.hasNext()) {
            debug();
        }
    }

    private void S() {
        if (!token.isEmpty() && token.charAt(0) == '{') {
            eat("{");
            L();
            eat("}");
        } else if (token.length() >= 18 && token.substring(0, 18).equals("System.out.println")) {
            eat("System.out.println");
            eat("(");
            E();
            eat(")");
            eat(";");
        } else if (token.length() >= 2 && token.substring(0, 2).equals("if")) {
            eat("if");
            eat("(");
            E();
            eat(")");
            S();
            eat("else");
            S();
        } else if (token.length() >= 5 && token.substring(0, 5).equals("while")) {
            eat("while");
            eat("(");
            E();
            eat(")");
            S();
        } else {
            debug();
        }
    }

    private void L() {
        if (token.charAt(0) == '{' || 
            (token.length() >= 18 && token.substring(0, 18).equals("System.out.println")) || 
            (token.length() >= 2 && token.substring(0, 2).equals("if")) || 
            (token.length() >= 5 && token.substring(0, 5).equals("while"))) {
            S();
            L();
        }
    }

    private void E() {
        if (token.length() >= 4 && token.substring(0, 4).equals("true")) {
            eat("true");
        } else if (token.length() >= 5 && token.substring(0, 5).equals("false")) {
            eat("false");
        } else if (!token.isEmpty() && token.charAt(0) == '!') {
            eat("!");
            E();
        } else {
            debug();
        }
    }
}
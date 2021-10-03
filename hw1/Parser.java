import java.util.Scanner;

public class Parser {
    private Scanner sc;
    private String token;
    private boolean failed;
    public Parser() {
        sc = new Scanner(System.in);
        token = "";
        failed = false;
    }

    public void parse() {
        goal();
        if (!failed) {
            System.out.println("Program parsed successfully");
        }
    }

    private void debug() {
        if (!failed) {
            // System.out.println("failed: " + token);
            failed = true;
            System.out.println("Parse error");
        }
    }

    public void nextToken() {
        if (!token.equals("")) {
            debug();
        }
        if (sc.hasNext()) {
            token = sc.next();
        }
        // System.out.println(token);
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
            return;
        }
        S();
        if (sc.hasNext()) {
            debug();
        }
    }

    private void S() {
        if (failed) {
            return;
        }
        if (token.charAt(0) == '{') {
            eat("{");
            // if (token.isEmpty()) {
            //     nextToken();
            // }
            L();
            // if (token.isEmpty()) {
            //     nextToken();
            // }
            eat("}");
        } else if (token.length() >= 18 && token.substring(0, 18).equals("System.out.println")) {
            eat("System.out.println(");
            E();
            eat(");");
        } else if (token.equals("if")) {
            eat("if");
            // nextToken();
            eat("(");
            E();
            eat(")");
            // nextToken();
            S();
            // nextToken();
            eat("else");
            // nextToken();
            S();
        } else if (token.equals("while")) {
            eat("while");
            // nextToken();
            eat("(");
            E();
            eat(")");
            // nextToken();
            S();
        } else {
            debug();
        }
    }

    private void L() {
        if (failed) {
            return;
        }
        if (token.charAt(0) == '{' || 
            (token.length() >= 18 && token.substring(0, 18).equals("System.out.println")) || 
            token.equals("if") || 
            token.equals("while")) {
            S();
            // nextToken();
            L();
        }
    }

    private void E() {
        if (failed) {
            return;
        }
        if (token.length() >= 4 && token.substring(0, 4).equals("true")) {
            eat("true");
        } else if (token.length() >= 5 && token.substring(0, 5).equals("false")) {
            eat("false");
        } else if (token.charAt(0) == '!') {
            eat("!");
            E();
        } else {
            debug();
        }
    }
}
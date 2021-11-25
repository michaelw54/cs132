import cs132.IR.SparrowParser;
import cs132.IR.visitor.SparrowConstructor;
import cs132.IR.syntaxtree.Node;
import cs132.IR.sparrow.Program;
import cs132.IR.ParseException;

public class S2SV {
    public static void main(String[] args) {
        try {
            Node root = new SparrowParser(System.in).Program();
            SparrowConstructor sc = new SparrowConstructor();
            root.accept(sc);
            Program program = sc.getProgram();

            // KEVIN LI VERSION
            // KevinLivenessVisitor lv = new KevinLivenessVisitor();
            // program.accept(lv);
            // KevinTranslatorVisitor tv = new KevinTranslatorVisitor();
            // program.accept(tv, lv);
            // tv.addSaves();
            // for (int i = 0; i < tv.result.size(); ++i) {
            //     System.out.println(tv.result.get(i));
            // }

            // MY VERSION
            LivenessVisitor lv = new LivenessVisitor();
            program.accept(lv);
            program.accept(new MainVisitor(), lv);

            // System.out.println(lv.finalRegAssignments);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
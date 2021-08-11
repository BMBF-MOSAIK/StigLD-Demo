package de.dfki.asr.stigFN;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase4;

public class manhattanDistance extends FunctionBase4 {
    @Override
    public NodeValue exec(NodeValue x1, NodeValue y1, NodeValue x2, NodeValue y2) {
        System.out.println("Manhattan");
        System.out.println(Integer.parseInt(x1.toString()));
        int xp1 = x1.getInteger().intValue();
        int yp1 = y1.getInteger().intValue();
        int xp2 = x2.getInteger().intValue();
        int yp2 = y2.getInteger().intValue();
        int distance = Math.abs(xp1-xp2) + Math.abs(yp1-yp2);
        return NodeValue.makeInteger(distance);
    }
}

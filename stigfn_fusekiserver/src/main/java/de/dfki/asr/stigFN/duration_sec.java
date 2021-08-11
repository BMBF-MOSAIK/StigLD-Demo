package de.dfki.asr.stigFN;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class duration_sec extends FunctionBase2 {
    @Override
    public NodeValue exec(NodeValue nodeValue, NodeValue nodeValue1) {
        String t1 = ((nodeValue.toString()).split("\\^")[0].replace("T", " ").split("\\+")[0].split("\"")[1]);
        String t2 = ((nodeValue1.toString()).split("\\^")[0].replace("T", " ").split("\\+")[0].split("\"")[1]);
        DateFormat df = new java.text.SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSS");
        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df.parse(t1);
            date2 = df.parse(t2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double diff = (date2.getTime() - date1.getTime())/1000.0;

        return NodeValue.makeDouble(diff);
    }
}

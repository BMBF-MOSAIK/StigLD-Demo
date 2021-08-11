package de.dfki.asr.stigFN;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase4;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class exponentialDecay extends FunctionBase4 {
    @Override
    public NodeValue exec(NodeValue initTime, NodeValue currentTime, NodeValue decayRate, NodeValue initConcentration) {
        String t1 = ((initTime.toString()).split("\\^")[0].replace("T", " ").split("\\+")[0].split("\"")[1]);
        String t2 = ((currentTime.toString()).split("\\^")[0].replace("T", " ").split("\\+")[0].split("\"")[1]);
        DateFormat df = new java.text.SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = df.parse(t1);
            date2 = df.parse(t2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        double diff = (date2.getTime() - date1.getTime())/1000;
        try {
            double decay = decayRate.getDouble();
            double initConc = initConcentration.getDouble();
            double finalConc = initConc*Math.exp(-1*diff*decay);
            return NodeValue.makeDouble(finalConc);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}

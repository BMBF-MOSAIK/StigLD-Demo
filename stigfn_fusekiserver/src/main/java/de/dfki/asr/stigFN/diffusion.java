package de.dfki.asr.stigFN;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase5;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class diffusion extends FunctionBase5 {
    static double xpos, ypos;
    @Override
    public NodeValue exec(NodeValue xPos, NodeValue yPos, NodeValue duration, NodeValue concentration, NodeValue rate) {
        double L=10;
        double nx = 100;
        double T = duration.getDouble();            // can be taken from input of sparql function (duration)
        double nt = T;
        double alpha = rate.getDouble();            // taken from sparql function (rate)
        double conc = concentration.getDouble();    // initial concentration taken from sparql function (concentration)
        //double dist = distance.getDouble();         //distance from center, taken from sparql function (distance)
        xpos = xPos.getDouble();
        ypos = yPos.getDouble();
        double[] x = new double[(int)nx+1];
        linspace(x, 0, L, L / nx);
        double dx = x[1] - x[0];
        double[] t = new double[(int)nt+1];
        linspace(t, 0, T, T / nt);
        double dt = t[1] - t[0];
        double F = alpha*dt/(dx*dx);
        double fac = 1.0 - 2.0*F;
        double[] init_conc = new double[(int)nx+1];


        for(int i=0; i<init_conc.length; i++)
        {
            if(x[i]>=4.5 && x[i]<=5.5)
                init_conc[i] = conc*Math.sin(2*Math.PI*5*(x[i]-4.5)/L);
            else
                init_conc[i] = 0;
        }

        List<Double> xList = new ArrayList<>();
        for(double n:x){
            xList.add(n);
        }

        List<Double> y1 = new ArrayList<>();
        for(double n:init_conc){
            y1.add(n);
        }

        double[] conc_old = init_conc;
        double[] conc_new = new double[(int)nx+1];

        for(int j=0; j<nt; j++)
        {
            for (int i=1; i<conc_new.length-1; i++)
            {
                conc_new[i]= round(fac*conc_old[i]+F*(conc_old[i-1]+conc_old[i+1]),8);
            }
            conc_old = conc_new;
        }

        List<Double> y = new ArrayList<>();
        for(double n:conc_new){
            y.add(n);
        }

//        System.out.println("Final concentration at distance "+distance+" from point of pheromone deposition after "+T+" seconds is "+conc_new[(int)(((L/2)-dist)*(nx/L))]);
        double output1 = conc_new[(int)(((L/2)-0)*(nx/L))];

        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(new FileInputStream("model2.ttl"), null, "TTL");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String qs="";
        for(int i = 0; i < 5; i++) {
            double output = conc_new[(int) (((L / 2) - i) * (nx / L))];
            qs = getUpdateQuery(output, i);
            UpdateRequest request = UpdateFactory.create(qs);
            UpdateAction.execute(request, model);
        }

        try (OutputStream out = new FileOutputStream("model2.ttl")) {
            RDFDataMgr.write(out, model, Lang.TTL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n------------------------\n"+qs+"\n------------------------\n");

        return NodeValue.makeDouble(output1);
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    private static void linspace(double[] array, double start, double end, double space)
    {
        array[0] = start;
        for(int i=1; i<array.length; i++) {
            array[i] = round(array[i-1]+space,1);
        }
    }
    private static String getUpdateQuery(double output, int i)
    {
        return "PREFIX ex: <http://example.org/> \n" +
                "PREFIX http: <http://www.w3.org/2011/http#>\n" +
                "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "prefix xsd:   <http://www.w3.org/2001/XMLSchema#> \n" +
                "prefix stigma: <http://www.dfki.de/asr/property/stigma#>\n" +
                "prefix foaf:  <http://xmlns.com/foaf/0.1/> \n" +
                "prefix point: <http://gridPoint/> \n" +
                "prefix st:  <http://www.dfki.de/asr/property#> \n" +
                "prefix pos:  <http://www.dfki.de/asr/property/position#> \n" +
                "DELETE{\n" +
                "  ?topos2 stigma:hasConcentration ?v0.\n" +
                "}\n" +
                "INSERT{\n" +
                "  ?topos2 stigma:hasConcentration \""+output+"\"^^xsd:double.\n" +
                "}\n" +
                "WHERE\n" +
                "{\n" +
                "  ?mach  rdf:type  st:Artifact;\n" +
                "         st:artifactType  ex:Production;\n" +
                "         ex:located  ?topos.\n" +
                "  ?topos pos:xPos ?x1;\n" +
                "         pos:yPos ?y1.      \n" +
                "  \n" +
                "  ?topos2 a st:Topos ;\n" +
                "          stigma:hasConcentration ?v0; \n" +
                "          pos:xPos ?x2;\n" +
                "          pos:yPos ?y2.\n" +
                "  BIND(abs(?x2-?x1)+abs(?y2-?y1) as ?dist) \n" +
                "\n" +
                "  FILTER(?dist = "+i+")\n" +
                "}";
    }
}






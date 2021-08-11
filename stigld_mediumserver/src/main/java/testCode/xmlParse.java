package testCode;
import org.apache.jena.assembler.Mode;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class xmlParse {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        String open_orders = "prefix ex:    <http://example.org/>\n" +
                "\n" +
                "ASK {\n" +
                "  {SELECT ?order WHERE { ?order a  ex:Order . }}\n" +
                "  UNION {SELECT ?pickup WHERE { ?pickup a ex:PickupTask . }}\n" +
                "  UNION {SELECT ?work WHERE { ?work a ex:WorkstationTask  . }}\n" +
                "}\n";

        String count_artifacts = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix ex:    <http://example.org/>\n" +
                "prefix :      <http://example.org/benchmark/>\n" +
                "\n" +
                "SELECT ?n_w ?n_t ?n_o\n" +
                "WHERE {\n" +
                "\n" +
                "  {SELECT (COUNT(?w) as ?n_w ) {\n" +
                "      ?w a ex:ProductionArtifact .\n" +
                "  }}\n" +
                "  {SELECT (COUNT(?t) as ?n_t ) {\n" +
                "      ?t a ex:Transporter .\n" +
                "  }}\n" +
                "  {SELECT (COUNT(?o) as ?n_o ) {\n" +
                "      ?o a ex:Order .\n" +
                "  }}\n" +
                "}";

        String transporter_pos = "PREFIX ex:<http://example.org/>\n" +
                "PREFIX st:    <http://example.org/stigld/>\n" +
                "PREFIX pos:   <http://example.org/property/position#>\n" +
                "\n" +
                "SELECT ?t ?x ?y WHERE {\n" +
                "  ?t a ex:Transporter ; ex:located [ a st:Topos ; pos:xPos ?x ; pos:yPos ?y ] .\n" +
                "}\n";

        String workstation_tasks = "PREFIX ex:<http://example.org/>\n" +
                "\n" +
                "SELECT * WHERE {\n" +
                "  {SELECT ?machine (COUNT(?task) as ?n) WHERE {\n" +
                "      ?machine a ex:ProductionArtifact ;\n" +
                "      ex:queue ?task.\n" +
                "      ?task a ex:WorkstationTask .\n" +
                "  } GROUP BY ?machine }\n" +
                "}\n";

        Query openOrd_query = QueryFactory.create(open_orders);
        Query transporterPos_query = QueryFactory.create(transporter_pos);
        Query workTask_query = QueryFactory.create(workstation_tasks);
        Query countArtif_query = QueryFactory.create(count_artifacts);
        Integer t=0, o=0, w = 0, m_n=0, t_x=0, t_y=0;
        String machinea="";
        String transporter="";
        Map machine_num = new HashMap();
        Map<String,List<Integer>>  transp_x_y= new HashMap<String, List<Integer> >();
        try {
            QueryExecution qexec_openOrd = QueryExecutionFactory.createServiceRequest("http://localhost:3230/ds/", openOrd_query);
            QueryExecution qexec_transpPos = QueryExecutionFactory.createServiceRequest("http://localhost:3230/ds/", transporterPos_query);
            QueryExecution qexec_workTask = QueryExecutionFactory.createServiceRequest("http://localhost:3230/ds/", workTask_query);
            QueryExecution qexec_countArtif = QueryExecutionFactory.createServiceRequest("http://localhost:3230/ds/", countArtif_query);
            boolean res_openOrd = qexec_openOrd.execAsk() ;
            ResultSet res_countArtif = qexec_countArtif.execSelect();
            ResultSet res_workTask = qexec_workTask.execSelect();
            ResultSet res_transpPos = qexec_transpPos.execSelect();

            for ( ; res_countArtif.hasNext() ; )
            {
                QuerySolution soln = res_countArtif.nextSolution() ;

                RDFNode n_w = soln.get("?n_w") ;
                RDFNode n_t = soln.get("?n_t") ;
                RDFNode n_o = soln.get("?n_o") ;

                if ( n_w.isLiteral() )
                    w = Integer.parseInt(((Literal)n_w).getLexicalForm()) ;
                if ( n_t.isLiteral() )
                    t = Integer.parseInt(((Literal)n_t).getLexicalForm()) ;
                if ( n_o.isLiteral() )
                    o = Integer.parseInt(((Literal)n_o).getLexicalForm()) ;

            }

            for ( ; res_workTask.hasNext() ; )
            {
                QuerySolution soln = res_workTask.nextSolution() ;
                RDFNode machine = soln.get("?machine") ;
                RDFNode n = soln.get("?n") ;

                if ( machine.isResource() )
                {
                    Resource r = (Resource)machine ;
                    if ( ! r.isAnon() )
                    {
                        machinea = r.getURI();
                    }
                }
                if ( n.isLiteral() )
                    m_n = Integer.parseInt(((Literal)n).getLexicalForm()) ;
                machine_num.put(machinea, m_n);
            }

            for ( ; res_transpPos.hasNext() ; )
            {
                QuerySolution soln = res_transpPos.nextSolution() ;

                RDFNode tr = soln.get("?t") ;
                RDFNode x = soln.get("?x") ;
                RDFNode y = soln.get("?y") ;

                if ( tr.isResource() )
                {
                    Resource r = (Resource)tr ;
                    if ( ! r.isAnon() )
                    {
                        transporter = r.getURI();
                    }
                }
                if(tr.isLiteral())
                    System.out.println(((Literal)tr).getLexicalForm());
                if ( x.isLiteral() )
                    t_x = Integer.parseInt(((Literal)x).getLexicalForm()) ;
                if ( y.isLiteral() )
                    t_y = Integer.parseInt(((Literal)y).getLexicalForm()) ;
                List<Integer> values = new ArrayList<Integer>();
                values.add(t_x);
                values.add(t_y);
                transp_x_y.put(transporter, values);
            }
            System.out.println(machine_num);
            System.out.println(transp_x_y);
            System.out.println("W "+w+" T "+t+" O "+o);
            System.out.println(res_openOrd);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package de.dfki.StigLD;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.dfki.StigLD.Benchmark.Benchmark;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@RestController
@RequestMapping("/sparql/")
public class Controller {

    private Benchmark benchmark = new Benchmark();

    @GetMapping("/getModel")
    public String getModel() throws IOException, UnirestException {
        //initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-query")
                .header("Accept", "text/turtle")
                .body(getAllTriples)
                .asString();
        String resp;
        try{
            resp = response.getBody();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Model model = ModelFactory.createDefaultModel();
        model.read(IOUtils.toInputStream(resp, "UTF-8"), null, "TTL");
        model.write(stream, "TTL");
        String ret = stream.toString();
        return ret;
    }
    @GetMapping(value="/json", produces="application/json")
    public String json() throws IOException, UnirestException {
        //initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-query")
                .header("Accept", "text/turtle")
                .body(getAllTriples)
                .asString();
        String resp;
        try{
            resp = response.getBody();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        Model model = ModelFactory.createDefaultModel();
        model.read(IOUtils.toInputStream(resp, "UTF-8"), null, "TTL");
	return new JSON_Serializer().getModelAsJson(model);
    }


    @PostMapping("/simEnd")
    public String query1(@RequestBody String query) throws IOException, UnirestException {

        return  "Hello";
    }


    @PostMapping("/query")
    public String query(@RequestBody String query) throws IOException, UnirestException {
        initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-query")
                .header("Accept", "text/turtle")
                .body(query)
                .asString();
        String resp;
        try{
            resp = response.getBody().toString();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        return  resp;
    }

    @PostMapping("/update")
    public String postUpdate(@RequestBody String query) throws IOException, UnirestException {
	LocalDateTime before = LocalDateTime.now();
	initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(query)
                .asString();
	LocalDateTime after = LocalDateTime.now();
	benchmark.lastQueryTime(before.until(after, ChronoUnit.MILLIS));
	benchmark.measure();
        String resp;
        try{
            resp = response.getBody().toString();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
//            System.out.println(e.getMessage());
        }
        return  resp;
    }


    @PostMapping("/evolve")
    public String evolve(@RequestBody String query) throws IOException, UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(evolve)
                .asString();
        String resp;
        try{
            resp = response.getBody().toString();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        return  resp;
    }

    public void initEvolve() throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response1 = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(evolve)
                .asString();
//        d_evolve();
        HttpResponse<String> response2 = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(diff_evolve)
                .asString();
        HttpResponse<String> response3 = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(deleteStigma)
                .asString();
    }

    public String evolve = "PREFIX ex:<http://example.org/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX st:  <http://example.org/stigld/>\n" +
            "PREFIX stigFN: <http://www.dfki.de/func#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "\n" +
            "DELETE {\n" +
            "  ?stigma a ex:NegFeedback ; ?p ?o .\n" +
            "  ?topos st:carries ?stigma .\n" +
            "}\n" +
            "  INSERT {\n" +
            "  ?topos st:carries ?new .\n" +
            "  ?new a st:Stigma, ex:NegFeedback ; st:level ?c ; st:created ?now ; st:decayRate ?d .\n" +
            "}\n" +
            "WHERE {\n" +
            "  BIND(NOW() as ?now)\n" +
            "  ?topos a st:Topos ; st:carries ?stigma .\n" +
            "  ?stigma a ex:NegFeedback ; st:decayRate ?d ; ?p ?o .\n" +
            "  FILTER (isBlank(?stigma))\n" +
            "\n" +
            "  {SELECT distinct ?topos (BNODE() as ?new) WHERE {\n" +
            "    ?topos a st:Topos .\n" +
            "    FILTER EXISTS { ?topos st:carries [ a ex:NegFeedback ] .}\n" +
            "  }}\n" +
            "\n" +
            "  {SELECT (SUM(?c_i) as ?c) ?topos WHERE {\n" +
            "    ?topos st:carries [ a ex:NegFeedback ; st:level ?lvl; st:created ?then; st:decayRate ?d ].\n" +
            "    BIND(stigFN:linear_decay(?then, now(), ?d, ?lvl) as ?c_i)\n" +
            "  } GROUP BY ?topos}\n" +
            "};\n" +
            "DELETE\n" +
            "{\n" +
            "    ?topos st:carries ?stigma.\n" +
            "    ?stigma ?p ?o.\n" +
            "}\n" +
            "WHERE{\n" +
            "    ?topos a st:Topos ; st:carries ?stigma.\n" +
            "    ?stigma a st:Stigma; st:level ?lvl; ?p ?o.\n" +
            "    FILTER (isBlank(?stigma))\n" +
            "    FILTER(?lvl=0)\n" +
            "}";


    public String diff_evolve = "PREFIX ex:<http://example.org/>\n"
            + "PREFIX pos: <http://example.org/property/position#>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX st:  <http://example.org/stigld/>\n"
            + "PREFIX stigFN: <http://www.dfki.de/func#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "\n"
            + "########## REMOVE OLD DIFFUSION TRACES\n"
            + "\n"
            + "DELETE {\n"
            + "    ?existing st:carries ?ex .\n"
            + "    ?ex a st:DiffusionTrace ; ?p ?o .\n"
            + "}\n"
            + "WHERE{\n"
            + "    ?existing st:carries ?ex .\n"
            + "    ?ex a st:DiffusionTrace ; ?p ?o .\n"
            + "    FILTER(isBlank(?ex))\n"
            + "} ;\n"
            + "\n"
            + "########## CALCULATE NEW DIFFUSION TRACES\n"
            + "\n"
            + "DELETE {\n"
            + "    ?stigma st:level ?srcLevel .\n"
            + "}\n"
            + "INSERT {\n"
            + "    ?aoe st:carries [ a st:Stigma , ex:DiffusionTrace ; st:level ?diffusion ; ex:diffusionSource ?source ] .\n"
            + "    ?stigma st:level ?sourceDiffusion .\n"
            + "}\n"
            + "WHERE {\n"
            + "\n"
            + "    ?source  a st:Topos; pos:xPos ?source_x; pos:yPos ?source_y; st:carries ?stigma.\n"
            + "    ?stigma a ex:TransportStigma; st:created ?then; st:decayRate ?decay ; st:level ?srcLevel .\n"
            + "    ?aoe  a st:Topos;    pos:xPos ?effect_x;    pos:yPos ?effect_y.\n"
            + "\n"
            + "    BIND(NOW() AS ?now)\n"
            + "    BIND(stigFN:duration_secs(?then, ?now) AS ?duration)\n"
            + "    BIND(abs(?effect_x-?source_x) + abs(?effect_y-?source_y) AS ?dist)\n"
            + "    BIND(stigFN:diffusion_1D(?stigma, ?dist, ?duration, ?srcLevel, ?decay) AS ?diffusion)\n"
            + "    BIND(0 as ?sourceDist)\n"
            + "    BIND(stigFN:diffusion_1D(?stigma, ?sourceDist, ?duration, ?srcLevel, ?decay) AS ?sourceDiffusion)\n"
            + "    FILTER(?dist > 0 && ?dist < 10 )\n"
            + "} ;\n"
            + "\n"
            + "########## AGGREGATE DIFFUSION TRACES FROM SAME SOURCES\n"
            + "\n"
            + "DELETE {\n"
            + "	?topos st:carries ?old .\n"
            + "	?old a st:Stigma , ex:DiffusionTrace ; st:level ?oldLevel .\n"
            + "}\n"
            + "INSERT {\n"
            + "	?topos st:carries ?stigma .\n"
            + "	?stigma a st:Stigma , ex:DiffusionTrace ; st:level ?c ; ex:diffusionSource ?source.\n"
            + "}\n"
            + "WHERE {\n"
            + "	?topos st:carries ?old .\n"
            + "	?old a st:Stigma , ex:DiffusionTrace ; st:level ?oldLevel ; ex:diffusionSource ?source .\n"
            + "  FILTER(isBlank(?old))\n"
            + "	{SELECT (SUM(?lvl) as ?c) (BNODE() as ?stigma) ?topos ?source WHERE {\n"
            + "		?topos a st:Topos ; st:carries [a st:Stigma , ex:DiffusionTrace; st:level ?lvl ; ex:diffusionSource ?source ].\n"
            + "	} GROUP BY ?topos ?source }\n"
            + "};\n"
            + "################### REMOVE STRAY BLANK NODES##############\n"
            +"DELETE \n"
            +"{\n"
            +" ?o  ex:diffusionSource  ?o1  . \n"
            +"}\n"
            +"WHERE {\n"
            +"        ?o  ex:diffusionSource  ?o1  .\n"
            +"        FILTER (isBlank(?o)) \n"
            +"        FILTER NOT EXISTS{?o ^st:carries ?t}\n"
            +"}";

    public void d_evolve()
    {
        String get =   "    PREFIX ex:<http://example.org/>\n" +
                "    PREFIX pos: <http://example.org/property/position#>\n" +
                "    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "    PREFIX st:  <http://example.org/stigld/>\n" +
                "    PREFIX stigFN: <http://www.dfki.de/func#>\n" +
                "    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    SELECT ?dist ?diffusion\n" +
                "    WHERE {\n" +
                "        {\n" +
                "            SELECT  DISTINCT ?then ?dist ?srcLevel ?decay\n" +
                "            {\n" +
                "                ?source  a st:Topos; pos:xPos ?source_x; pos:yPos ?source_y; st:carries ?stigma.\n" +
                "                ?stigma a ex:TransportStigma; st:created ?then; st:decayRate ?decay ; st:level ?srcLevel .\n" +
                "                ?aoe  a st:Topos;    pos:xPos ?effect_x;    pos:yPos ?effect_y.\n" +
                "                BIND(abs(?effect_x-?source_x) + abs(?effect_y-?source_y) AS ?dist)\n" +
                "            }\n" +
                "        }\n" +
                "        BIND(NOW() AS ?now)\n" +
                "        BIND(stigFN:duration_secs(?then, ?now) AS ?duration)\n" +
                "        \n" +
                "        BIND(stigFN:diffusion_1D(?dist, ?duration, ?srcLevel, ?decay) AS ?diffusion)\n" +
                "        FILTER(?dist >= 0 && ?dist < 10 )\n" +
                "    } order by asc (?dist)\n";
        String url = "http://localhost:" + 3230 + "/ds/";
        double output[] = new double[10];
        int i = 0;
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(url, get) ) {
            ResultSet rs = qExec.execSelect() ;
//            ResultSetFormatter.out(rs);
            for ( ; rs.hasNext() ; )
            {
                QuerySolution soln = rs.nextSolution() ;
                RDFNode p = soln.get("diffusion") ; // "x" is a variable in the query
                if ( p.isLiteral() )
                {
                    output[i]  = (double)((Literal)p).getValue();
                }
                String upd = getUpdate(output[0], output[i], i++);
//                System.out.println(output[0]+"----------"+output[i++]);
                RDFConnection conn = RDFConnectionFactory.connect(url);
                UpdateRequest request = UpdateFactory.create(upd);
                conn.update(request);
            }
        }
    }

    public String getUpdate(double k, double m, double dist){
        String re = "    PREFIX ex:<http://example.org/>\n" +
                "    PREFIX pos: <http://example.org/property/position#>\n" +
                "    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "    PREFIX st:  <http://example.org/stigld/>\n" +
                "    PREFIX stigFN: <http://www.dfki.de/func#>\n" +
                "    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "    ########## REMOVE OLD DIFFUSION TRACES\n" +
                "\n" +
                "    DELETE {\n" +
                "        ?existing st:carries ?ex .\n" +
                "        ?ex a st:DiffusionTrace ; ?p ?o .\n" +
                "    }\n" +
                "    WHERE{\n" +
                "        ?existing st:carries ?ex .\n" +
                "        ?ex a st:DiffusionTrace ; ?p ?o .\n" +
                "        FILTER(isBlank(?ex))\n" +
                "    } ;\n" +
                "\n" +
                "    ########## ADD NEW DIFFUSION TRACES\n" +
                "\n" +
                "    DELETE {\n" +
                "        ?stigma st:level ?srcLevel .\n" +
                "    }\n" +
                "    INSERT {\n" +
                "        ?aoe st:carries [ a st:Stigma , ex:DiffusionTrace ; st:level ?diffusion ; ex:diffusionSource ?source ] .\n" +
                "        ?stigma st:level ?sourceDiffusion .\n" +
                "    }\n" +
                "    WHERE {\n" +
                "\n" +
                "        ?source  a st:Topos; pos:xPos ?source_x; pos:yPos ?source_y; st:carries ?stigma.\n" +
                "        ?stigma a ex:TransportStigma; st:created ?then; st:decayRate ?decay ; st:level ?srcLevel .\n" +
                "        ?aoe  a st:Topos;    pos:xPos ?effect_x;    pos:yPos ?effect_y.\n" +
                "\n" +
                "        BIND(NOW() AS ?now)\n" +
                "        BIND(stigFN:duration_secs(?then, ?now) AS ?duration)\n" +
                "        BIND(abs(?effect_x-?source_x) + abs(?effect_y-?source_y) AS ?dist)\n" +
                "        BIND("+k+ " AS ?sourceDiffusion)\n" +
                "        BIND("+m+ " AS ?diffusion)\n" +
                "        FILTER(?dist = "+dist+")\n" +
                "    } ;\n" +
                "\n" +
                "    ########## AGGREGATE DIFFUSION TRACES FROM SAME SOURCES\n" +
                "\n" +
                "    DELETE {\n" +
                "        ?topos st:carries ?old .\n" +
                "        ?old a st:Stigma , ex:DiffusionTrace ; st:level ?oldLevel .\n" +
                "    }\n" +
                "    INSERT {\n" +
                "        ?topos st:carries ?stigma .\n" +
                "        ?stigma a st:Stigma , ex:DiffusionTrace ; st:level ?c ; ex:diffusionSource ?source.\n" +
                "    }\n" +
                "    WHERE {\n" +
                "        ?topos st:carries ?old .\n" +
                "        ?old a st:Stigma , ex:DiffusionTrace ; st:level ?oldLevel ; ex:diffusionSource ?source .\n" +
                "      FILTER(isBlank(?old))\n" +
                "        {SELECT (SUM(?lvl) as ?c) (BNODE() as ?stigma) ?topos ?source WHERE {\n" +
                "            ?topos a st:Topos ; st:carries [a st:Stigma , ex:DiffusionTrace; st:level ?lvl ; ex:diffusionSource ?source ].\n" +
                "        } GROUP BY ?topos ?source }\n" +
                "    };\n" +
                "    ################### REMOVE STRAY BLANK NODES##############\n" +
                "    DELETE\n" +
                "    {\n" +
                "     ?o  ex:diffusionSource  ?o1  .\n" +
                "    }\n" +
                "    WHERE {\n" +
                "            ?o  ex:diffusionSource  ?o1  .\n" +
                "            FILTER (isBlank(?o))\n" +
                "            FILTER NOT EXISTS{?o ^st:carries ?t}\n" +
                "    }";
        return re;
    }

            public String getAllTriples = "prefix ord:   <http://example.org/orders#>\n" +
                    "prefix st:    <http://example.org/stigld/> \n" +
                    "prefix ex:    <http://example.org/> \n" +
                    "prefix law:   <http://example.org/rules#> \n" +
                    "prefix task:  <http://example.org/tasks#> \n" +
                    "prefix pos:   <http://example.org/property/position#> \n" +
                    "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix xsd:   <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix en:    <http://example.org/entities#> \n" +
                    "prefix topos: <http://example.org/gridPoint/> \n"+
                    "DESCRIBE * WHERE {?s ?p ?o.}\n";

            public String deleteStigma ="PREFIX ex:<http://example.org/>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                    "PREFIX st:  <http://example.org/stigld/>\n" +
                    "PREFIX stigFN: <http://www.dfki.de/func#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "DELETE\n" +
                    "{\n" +
                    "    ?topos st:carries ?stigma.\n" +
                    "    ?stigma ?p ?o.\n" +
                    "}\n" +
                    "WHERE{\n" +
                    "    ?topos a st:Topos ; st:carries ?stigma.\n" +
                    "    ?stigma a st:Stigma; st:level ?lvl; ?p ?o.\n" +
                    "    FILTER (isBlank(?stigma))\n" +
                    "    FILTER(?lvl=0)\n" +
                    "}";
           
}

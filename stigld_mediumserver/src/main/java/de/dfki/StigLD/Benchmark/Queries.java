/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

/**
 *
 * @author tospie
 */
public class Queries {
	    public static String open_orders = "prefix ex:    <http://example.org/>\n" +
                    "\n" +
                    "ASK {\n" +
                    "  {SELECT ?order WHERE { ?order a  ex:Order . }}\n" +
                    "  UNION {SELECT ?pickup WHERE { ?pickup a ex:PickupTask . }}\n" +
                    "  UNION {SELECT ?work WHERE { ?work a ex:WorkstationTask  . }}\n" +
                    "UNION {SELECT ?product WHERE { ?product a ex:Product  . }}\n" +
                    "}\n";

            public static String count_artifacts = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
		    "prefix ex:    <http://example.org/>\n" +
		    "prefix :      <http://example.org/benchmark/>\n" +
		    "\n" +
		    "SELECT * WHERE {\n" +
		    "\n" +
		    "  {SELECT (COUNT(?w) as ?workstations ) {\n" +
		    "      ?w a ex:ProductionArtifact .\n" +
		    "  }}\n" +
		    "  {SELECT (COUNT(?t) as ?transporters ) {\n" +
		    "      ?t a ex:Transporter .\n" +
		    "  }}\n" +
		    "  {SELECT (COUNT(?o) as ?orders ) {\n" +
		    "      ?o a ex:Order .\n" +
		    "  }}\n" +
		    "}" ;

            public static String transporter_pos = "PREFIX ex:<http://example.org/>\n" +
                    "PREFIX st:    <http://example.org/stigld/>\n" +
                    "PREFIX pos:   <http://example.org/property/position#>\n" +
                    "\n" +
                    "SELECT ?t ?x ?y WHERE {\n" +
                    "  ?t a ex:Transporter ; ex:located [ a st:Topos ; pos:xPos ?x ; pos:yPos ?y ] .\n" +
                    "}\n";

	    public static String workstation_tasks = "PREFIX ex:<http://example.org/>\n" +
		    "\n" +
		    "SELECT ?ws ?startTime WHERE {\n" +
		    "      ?ws a ex:ProductionArtifact ;   ex:queue ?task.    \n" +
		    "      ?task a ex:WorkstationTask ; ex:startTime ?startTime .\n" +
		    "}";

	    public static String workstation_task_counts = "PREFIX ex:<http://example.org/>\n" +
		    "\n" +
		    "SELECT * WHERE {\n" +
		    "  {SELECT ?machine (COUNT(?task) as ?n) WHERE {\n" +
		    "      ?machine a ex:ProductionArtifact ;\n" +
		    "      ex:queue ?task.\n" +
		    "      ?task a ex:WorkstationTask .\n" +
		    "  } GROUP BY ?machine }\n" +
		    "}";

	    public static String makeshift_times = "PREFIX ex:<http://example.org/>\n" +
		    "\n" +
		    "SELECT ?avg ?median ?min ?max WHERE {\n" +
		    "      ex:MakeshifttimesStatistics ex:avg ?avg ; ex:median ?median ; ex:minMakeshift ?min ; ex:maxMakeshift ?max ." +
		    "}";
}

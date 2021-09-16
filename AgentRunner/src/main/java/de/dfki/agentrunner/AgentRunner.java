/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.agentrunner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mech01
 */
public class AgentRunner {
    
    public static String All_query = "prefix ex:    <http://example.org/>\n" +
"prefix st:    <http://example.org/stigld/>\n" +
"prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
"prefix xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
"prefix en:    <http://example.org/entities#>\n" +
"PREFIX stigFN: <http://www.dfki.de/func#>\n" +
"######################PERFORM PRODUCTION####################################\n" +
"DELETE {\n" +
"    ?task ?p1 ?o .\n" +
"    ?s ?p2 ?task .\n" +
"    ?order ex:status ?status.\n" +
"}\n" +
"INSERT {\n" +
"    ?id a ex:Product ; ex:Order ?order ; ex:startTime ?taskStartTime ;\n" +
"    ex:located ?target ;\n" +
"    ex:created ?now .\n" +
"    ?order ex:status \"pickup\"^^xsd:string.\n" +
"\n" +
"    ?target st:carries ?marker .\n" +
"    ?marker a st:Stigma, ex:TransportStigma ; st:created ?now ;\n" +
"      st:decayRate \"0.03\"^^xsd:double ; st:level \"100.0\"^^xsd:double .\n" +
"}\n" +
"WHERE {\n" +
"  BIND (NOW() as ?now)\n" +
"  { SELECT * WHERE {\n" +
"      ?machine ex:queue ?task .\n" +
"      { SELECT (UUID() as ?id) (UUID() as ?marker) ?task WHERE {\n" +
"          ?task a ex:WorkstationTask ; ex:endTime ?endTime .\n" +
"          FILTER(?endTime < NOW())}}\n" +
"  }}\n" +
"\n" +
"  {SELECT (COUNT(?done) as ?waiting) ?output WHERE {\n" +
"    ?output a ex:Port ; ex:located ?pos .\n" +
"    OPTIONAL {?done a ex:Product ; ex:located ?pos .}\n" +
"  } GROUP BY ?output}\n" +
"\n" +
"  ?machine a ex:ProductionArtifact ; ex:queue ?task ; ex:outputPort ?output .\n" +
"  ?output ex:capacity ?capacity ; ex:located ?target.\n" +
"\n" +
"  OPTIONAL { ?task ?p1 ?o . }\n" +
"  OPTIONAL { ?s ?p2 ?task . }\n" +
"\n" +
"  ?task ex:order ?order ; ex:startTime ?taskStartTime .\n" +
"  ?order ex:status ?status .\n" +
"\n" +
"  FILTER(?waiting < ?capacity)\n" +
"};\n" +
"##################################### PERFORM MOVE ##########################################\n" +
"DELETE {\n" +
"  ?t ex:located ?from ; ex:status ex:busy ; ex:queue ?task .\n" +
"  ?task a ex:MoveTask ; ex:startTime ?start ; ex:endTime ?end ; ex:toLocation ?to .\n" +
"}\n" +
"INSERT {\n" +
"  ?t ex:located ?finalTO ; ex:status ex:idle .\n" +
"}\n" +
"WHERE {\n" +
"  ?t a ex:Transporter ; ex:located ?from ; ex:queue ?task; ex:idlePosition ?idle.\n" +
"  ?task a ex:MoveTask ; ex:startTime ?start ; ex:endTime ?end ; ex:toLocation ?to .\n" +
"  OPTIONAL {?t1 a ex:Transporter; ex:located ?to}\n" +
"  BIND(IF(BOUND(?t1), ?from, ?to) as ?finalTO)\n" +
"  FILTER (isBlank(?task))\n" +
"  FILTER (NOW() > ?end )\n" +
"};\n" +
"##############################ASSIGN MOVE########################################\n" +
"DELETE {\n" +
"?t ex:status ex:idle .\n" +
"}\n" +
"INSERT {\n" +
"?t ex:queue ?task ; ex:status ex:busy .\n" +
"?task a ex:MoveTask ; ex:startTime ?now ; ex:endTime ?endTime ; ex:toLocation ?to .\n" +
"}\n" +
"WHERE {\n" +
"SELECT (BNODE() as ?task) ?now ?endTime ?t ?from ?to WHERE {\n" +
"\n" +
" ?t a ex:Transporter ; ex:located ?from ; ex:speed ?speed ; ex:status ex:idle .\n" +
"{SELECT (SAMPLE(?adj) as ?to) ?t WHERE {\n" +
"?t ex:located ?from .\n" +
"{SELECT ?adj ?from WHERE {\n" +
"?from st:adjacentTo ?adj.\n" +
"?adj st:carries [ a ex:TransportStigma ; st:level ?lvl ].\n" +
"FILTER NOT EXISTS {\n" +
"?from st:adjacentTo ?otherAdj.\n" +
"?otherAdj st:carries [ a ex:TransportStigma ; st:level ?otherLvl ].\n" +
"FILTER(?otherLvl > ?lvl) }\n" +
"} ORDER BY RAND() }\n" +
"} GROUP BY ?t}\n" +
"\n" +
" FILTER NOT EXISTS {\n" +
"?m ex:located ?from ; a ex:Product .\n" +
"}\n" +
"\n" +
" BIND (NOW() as?now)\n" +
"BIND (?now + \"PT10S\"^^xsd:duration * (1/?speed) as ?endTime)\n" +
"}} ;\n" +
"\n" +
"DELETE {\n" +
"?t ex:status ex:idle .\n" +
"}\n" +
"INSERT {\n" +
"?t ex:queue ?task ; ex:status ex:busy .\n" +
"?task a ex:MoveTask ; ex:startTime ?now ; ex:endTime ?endTime ; ex:toLocation ?to .\n" +
"}\n" +
"WHERE {\n" +
"SELECT DISTINCT (BNODE() as ?task) ?now ?endTime ?t ?from ?to WHERE {\n" +
"?from st:adjacentTo ?to.\n" +
"?to st:carries [ a ex:DiffusionTrace ; st:level ?lvl] .\n" +
"?t a ex:Transporter ; ex:located ?from ; ex:speed ?speed ; ex:status ex:idle .\n" +
"{SELECT DISTINCT (SAMPLE(?adj) as ?to) ?t WHERE {\n" +
"?t ex:located ?from .\n" +
"{SELECT DISTINCT ?adj ?from WHERE {\n" +
"?from st:adjacentTo ?adj.\n" +
"?adj st:carries [ a ex:DiffusionTrace ; st:level ?lvl ].\n" +
"FILTER NOT EXISTS {?something a ex:Artifact ; ex:located ?adj .}\n" +
"FILTER NOT EXISTS {\n" +
"?from st:adjacentTo ?otherAdj.\n" +
"FILTER NOT EXISTS {?something a ex:Artifact ; ex:located ?otherAdj .}\n" +
"?otherAdj st:carries [ a ex:DiffusionTrace ; st:level ?otherLvl ].\n" +
"FILTER(?otherLvl > ?lvl) }\n" +
"} ORDER BY RAND() }\n" +
"} GROUP BY ?t}\n" +
"\n" +
" FILTER NOT EXISTS {\n" +
"?m ex:located ?from ; a ex:Product .\n" +
"}\n" +
"BIND (NOW() as?now)\n" +
"BIND (?now + \"PT10S\"^^xsd:duration * (1/?speed) as ?endTime)\n" +
"}};\n" +
"##################################### CREATE PICKUP #########################################\n" +
"DELETE {\n" +
"  ?t ex:status ex:idle .\n" +
"}\n" +
"INSERT {\n" +
"  ?t ex:queue ?task  ; ex:status ex:busy .\n" +
"  ?task a ex:PickUpTask ; ex:startTime ?now ; ex:endTime ?endTime  .\n" +
"}\n" +
"WHERE {\n" +
"{SELECT (BNODE() as ?task) ?now ?endTime ?t ?factor WHERE {\n" +
"  BIND (NOW() as?now)\n" +
"  ?t a ex:Transporter ; ex:located ?from ; ex:speed ?speed ; ex:status ex:idle  .\n" +
"  {SELECT (COUNT(?product) as ?count) ?from WHERE{\n" +
"    ?port ex:located ?from; a ex:Port .\n" +
"    ?product ex:located ?from; a ex:Product .\n" +
"  } GROUP BY (?from)}\n" +
"  BIND (?now + \"PT5S\"^^xsd:duration*(?count) as ?endTime)\n" +
"}}};\n" +
"###################################### FINISH PICKUP #############################################\n" +
"DELETE {\n" +
" ?transporter ex:status ?status ; ex:located ?topos ; ex:queue ?task .\n" +
"\n" +
" ?product a ex:Product ; ex:located ?topos ;  ex:created ?productCreateTime ; ex:Order ?order ; ex:startTime ?taskStartTime .\n" +
" ?order a ex:Order ; ex:created ?orderCreateTime; ex:status ?o.\n" +
"\n" +
" ?task a ex:PickUpTask ; ex:endTime ?endTime ; ex:startTime ?startTime .\n" +
"\n" +
" ?topos st:carries ?stigma .\n" +
" ?stigma a st:Stigma , ex:TransportStigma ; st:created ?stigmaCreateTime ; st:decayRate ?decayRate ; st:level ?source_level .\n" +
" ?all_that st:carries ?trace .\n" +
" ?trace a st:Stigma , ex:DiffusionTrace ; ex:diffusionSource ?topos ; st:level ?diffusion_level .\n" +
"\n" +
"}\n" +
"INSERT {\n" +
"  ?transporter ex:status ex:idle ; ex:located ?idlePosition .\n" +
"  ex:Makeshifttimes ex:time ?makeshiftTime .\n" +
"}\n" +
"WHERE {\n" +
"  ?task a ex:PickUpTask ; ex:endTime ?endTime ; ex:startTime ?startTime .\n" +
"  ?transporter a ex:Transporter ; ex:status ?status ;\n" +
"    ex:located ?topos ; ex:idlePosition ?idlePosition ; ex:queue ?task .\n" +
"\n" +
"  ?product a ex:Product ; ex:located ?topos ; ex:created ?productCreateTime ; ex:Order ?order ; ex:startTime ?taskStartTime .\n" +
"  ?order a ex:Order ; ex:created ?orderCreateTime ; ex:status ?o.\n" +
"\n" +
"  ?stigma a st:Stigma , ex:TransportStigma ; st:created ?stigmaCreateTime ; st:decayRate ?decayRate ; st:level ?source_level .\n" +
"\n" +
"  ?topos st:carries ?stigma .\n" +
"  ?trace a st:Stigma , ex:DiffusionTrace ; ex:diffusionSource ?topos ; st:level ?diffusion_level .\n" +
"  ?all_that st:carries ?trace .\n" +
"  BIND(stigFN:duration_secs(?taskStartTime, now()) as ?makeshiftTime )\n" +
"  FILTER (isBlank(?trace))\n" +
"  FILTER(now() > ?endTime)\n" +
"} ;\n" +
"\n" +
"##################### CALCULATE AVERAGE STATISTICS OF MAKESHIFT TIMES\n" +
"\n" +
"DELETE {\n" +
"  ex:MakeshifttimesStatistics ex:avg ?oldAvg ; ex:median ?oldMedian ; ex:minMakeshift ?oldMin ; ex:maxMakeshift ?oldMax .\n" +
"}\n" +
"INSERT {\n" +
"  ex:MakeshifttimesStatistics ex:avg ?avg ; ex:median ?median ; ex:minMakeshift ?min ; ex:maxMakeshift ?max .\n" +
"}\n" +
"WHERE {\n" +
"\n" +
"  {SELECT (AVG(?makeshift) as ?avg) (MEDIAN(?makeshift) as ?median) (MIN(?makeshift) as ?min) (MAX(?makeshift) as?max) WHERE {\n" +
"        ex:Makeshifttimes ex:time ?makeshift .\n" +
"  }}\n" +
"\n" +
"  OPTIONAL {SELECT ?oldAvg ?oldMedian ?oldMin ?oldMax WHERE {\n" +
"    ex:MakeshifttimesStatistics ex:avg ?oldAvg ; ex:median ?oldMedian ; ex:minMakeshift ?oldMin ; ex:maxMakeshift ?oldMax .\n" +
"  }}\n" +
"\n" +
"}\n" ;
    
    
    public static String Assign_query = "PREFIX ex:<http://example.org/>\n" +
"PREFIX pos: <http://example.org/property/position#>\n" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
"PREFIX st:  <http://example.org/stigld/>\n" +
"Prefix point: <http://gridPoint/>\n" +
"PREFIX stigFN: <http://www.dfki.de/func#>\n" +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
"\n" +
"DELETE{\n" +
"    ?order ex:status ?o.\n" +
"}\n" +
"INSERT {\n" +
"?order a ex:Order; ex:status \"assigned\"^^xsd:string.\n" +
"?taskURI  a        ex:WorkstationTask ;  ex:startTime  ?createTime;  ex:endTime ?endTime; ex:order ?order .\n" +
"?machine ex:queue ?taskURI.\n" +
"?stigmaURI a     ex:NegFeedback , st:Stigma ; st:created  ?createTime ;\n" +
"  st:decayRate  \"0.5\"^^xsd:double ; st:level      \"100.0\"^^xsd:double .\n" +
"\n" +
"?topos st:carries ?stigmaURI.\n" +
"}\n" +
"WHERE{\n" +
"	{SELECT DISTINCT ?machine ?topos ?taskURI ?stigmaURI ?s WHERE {\n" +
"		BIND (BNODE() AS ?taskURI)  BIND (BNODE() AS ?stigmaURI)\n" +
"		?machine a ex:ProductionArtifact;	ex:located ?topos;\n" +
"		ex:outputPort [ a ex:Port; ex:located ?outputPort; ex:capacity ?capacity ] .\n" +
"\n" +
"		OPTIONAL {SELECT (COUNT(?done) as ?waiting) ?outputPort {\n" +
"			?done a ex:Product ; ex:located ?outputPort . } GROUP BY ?outputPort }\n" +
"\n" +
"		OPTIONAL {SELECT (COUNT(?sched) as ?scheduled) ?machine WHERE {\n" +
"			?machine ex:queue ?sched.	} GROUP BY ?machine }\n" +
"\n" +
"		OPTIONAL { ?topos st:carries [ a ex:NegFeedback ; st:level ?lvl] }\n" +
"		BIND(IF(bound(?lvl), ?lvl, 0) as ?l)\n" +
"		BIND(IF(bound(?waiting), ?waiting, 0) as ?w)\n" +
"		BIND(IF(bound(?scheduled), ?scheduled, 0) as ?s)\n" +
"		FILTER(?w+?s < ?capacity)\n" +
"\n" +
"		} ORDER BY ASC (?l) LIMIT 1}\n" +
"\n" +
"  { SELECT ?order ?o WHERE {\n" +
"    ?order a ex:Order ; ex:created ?created ; ex:status ?o.\n" +
"    FILTER(?o != \"pickup\"^^xsd:string)\n" +
"    FILTER NOT EXISTS { ?otherTask a ex:WorkstationTask ; ex:order ?order .}\n" +
"  } ORDER BY (?created) LIMIT 1}\n" +
"\n" +
"	BIND((NOW() + \"PT20S\"^^xsd:duration * ?s) as ?createTime)\n" +
"	BIND(?createTime + \"PT20S\"^^xsd:duration  as ?endTime)\n" +
"}";
    public static void main(String[] args) throws InterruptedException{
        System.out.println("Agent Runner will start in 30 seconds");
//        TimeUnit.SECONDS.sleep(30);
       while(true)
       {
           for(int i=0; i<4; i++){
               try{
               Unirest.setTimeouts(0, 0);
                HttpResponse<String> response2 = Unirest.post("http://localhost:8080/sparql/addOrders/")
                .header("Content-Type", "text/plain")
                .body(Assign_query)
                .asString();}
               catch(UnirestException e)
               {
                   System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))+" == Endpoint not running");
               }
                
                TimeUnit.SECONDS.sleep(1);
            }
           
           
           try{Unirest.setTimeouts(0, 0);  
            HttpResponse<String> response = Unirest.post("http://localhost:8080/sparql/update/")
            .header("Content-Type", "application/sparql-update")
            .body(All_query)
            .asString();}
            catch(UnirestException e)
            {
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))+" == Endpoint not running");
            }
           TimeUnit.SECONDS.sleep(1);
       }
    }
    
    
    

}

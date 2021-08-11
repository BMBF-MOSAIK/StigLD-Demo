/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author tospie
 */
public class JSON_Serializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    ResponseObject response;

    public class ResponseObject {

	@Getter
	private final Topos[][] topoi;

	@Getter
	private final Set<Order> orders = new HashSet<>();

	public ResponseObject(int x_size, int y_size) {
	    this.topoi = new Topos[y_size][x_size];
	}
    }

    public class Order {

	@Getter
	String id;

	@Getter
	String timestamp;
        
        @Getter 
        String status;

	public Order(String id, String timestamp, String status) throws ParseException {
	    this.id = id;
            timestamp = timestamp.replace("T"," ");
            SimpleDateFormat parser=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = parser.parse(timestamp.split("\\+")[0]);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM HH:mm:ss");
            String formattedDate = formatter.format(date);
            this.timestamp = formattedDate;
            if(status.equals("unassigned"))
                status = "Order created";
            else if(status.equals("assigned"))
                status = "In production";
            else if(status.equals("pickup"))
                status = "Completed, awaiting pickup";
            this.status = status;
	}
    }

    public class Topos {

	@Getter
	double NegFeedback;

	@Getter
	double TransportStigma;

	@Getter
	double DiffusionTrace;

	@Getter
	Machine Machine;

	@Getter
	Transporter Transporter;

	@Getter
	boolean outputPort;
    }

    public class Machine {

	@Getter
	int Orders;
	@Getter
	int Waiting;
    }

    public class Transporter {

	@Getter
	double TimeToPickup;

	@Getter
	double TimeToMove;
    }

    public String getModelAsJson(Model model) throws JsonProcessingException {

	QueryExecution q = QueryExecutionFactory.create(getTopoi, model);
	ResultSet r = q.execSelect();
	QuerySolution solution = r.next();
	int x = solution.getLiteral("max_x").getInt() + 1;
	int y = solution.getLiteral("max_y").getInt() + 1;
	response = new ResponseObject(x, y);

	setOrders(model);
	setNegativeFeedback(model);
	setTransport(model);
	setDiffusion(model);
	setMachines(model);
	setTransporters(model);
	setOutputPorts(model);
	return objectMapper.writeValueAsString(response);
    }

    private void setOrders(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getOrders, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(o -> {
	    String id = o.getResource("order").getLocalName();
	    String timestamp = o.getLiteral("timestamp").getString();
            String status = o.getLiteral("status").getString();
            try {
                response.orders.add(new Order(id, timestamp, status));
            } catch (ParseException ex) {
                Logger.getLogger(JSON_Serializer.class.getName()).log(Level.SEVERE, null, ex);
            }
	});
    }

    private void setNegativeFeedback(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getNegFeedback, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("lvl").getDouble();

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    response.topoi[y][x].NegFeedback = level;
	});
    }

    private void setTransport(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getTransportStigma, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("lvl").getDouble();

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    response.topoi[y][x].TransportStigma = level;
	});
    }

    private void setDiffusion(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getDiffusion, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("total").getDouble();

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    response.topoi[y][x].DiffusionTrace = level;
	});
    }

    private void setMachines(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getMachines, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    int scheduled = s.getLiteral("scheduled").getInt();
	    int waiting = s.getLiteral("waiting").getInt();

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    Machine m = new Machine();
	    m.Orders = scheduled;
	    m.Waiting = waiting;
	    response.topoi[y][x].Machine = m;
	});
    }

    private void setOutputPorts(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getOutputPorts, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    response.topoi[y][x].outputPort = true;
	});
    }

    private void setTransporters(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getTransporters, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {

	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double pickremaining = 0;
	    double moveremaining = 0;
	    if (s.contains("pickremaining")) {
		pickremaining = s.getLiteral("pickremaining").getDouble();
	    }
	    if (s.contains("moveremaining")) {
		moveremaining = s.getLiteral("moveremaining").getDouble();
	    }
	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    Transporter t = new Transporter();
	    t.TimeToPickup = pickremaining;
	    t.TimeToMove = moveremaining;
	    response.topoi[y][x].Transporter = t;
	});
    }

    private final String getTopoi = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT (MAX(?x) as ?max_x)  (MAX(?y) as ?max_y) WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y }";

    private final String getNegFeedback = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:NegFeedback ; st:level ?lvl ] }";

    private final String getTransportStigma = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:TransportStigma; st:level ?lvl ] }";

    private final String getDiffusion = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT  ?x ?y (SUM(?lvl) as ?total) where {\n"
	    + "  ?s a st:Topos;\n"
	    + "    pos:xPos ?x;\n"
	    + "    pos:yPos ?y;\n"
	    + "    st:carries ?stigma.\n"
	    + "  \n"
	    + "  ?stigma a ex:DiffusionTrace;\n"
	    + "          st:level ?lvl.\n"
	    + "} \n"
	    + "group by ?s ?x ?y";

    private final String getMachines = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + "\n"
	    + "SELECT DISTINCT ?scheduled ?waiting ?x ?y WHERE{\n"
	    + "                ?m a ex:ProductionArtifact ;  ex:located [ pos:xPos ?x ; pos:yPos ?y ] . \n"
	    + "                \n"
	    + "                {SELECT (COUNT(?q) as ?scheduled ) ?m WHERE {\n"
	    + "                    ?m a ex:ProductionArtifact.\n"
	    + "                    OPTIONAL { ?m ex:queue ?q.  }\n"
	    + "                } GROUP BY ?m }\n"
	    + "                \n"
	    + "                {SELECT (COUNT(?prod) as ?waiting ) ?m WHERE {\n"
	    + "                    ?m a ex:ProductionArtifact ; ex:outputPort ?o . \n"
	    + "                    ?o ex:located ?o_pos .\n"
	    + "                   OPTIONAL { ?prod a ex:Product ; ex:located ?o_pos .  }\n"
	    + "                } GROUP BY ?m }\n"
	    + "}";

    private final String getTransporters = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT DISTINCT ?t ?x ?y ?pickremaining ?moveremaining WHERE \n"
	    + "{  \n"
	    + "        ?t a ex:Transporter ; ex:located [ pos:xPos ?x ; pos:yPos ?y] . \n"
	    + "        OPTIONAL { \n"
	    + "            ?t ex:queue [a ex:PickUpTask ; ex:endTime ?end ] . \n"
	    + "            BIND(NOW() as ?now) \n"
	    + "            BIND(IF(?now > ?end, 0, (hours(?end-?now) * 24 * 60 + minutes(?end-?now) * 60 + seconds(?end-?now) ) ) as ?pickremaining ) \n"
	    + "        }    \n"
	    + "        OPTIONAL { \n"
	    + "            ?t ex:queue [a ex:MoveTask ; ex:endTime ?end ] . \n"
	    + "            BIND(NOW() as ?now) \n"
	    + "            BIND(IF(?now > ?end, 0, (hours(?end-?now) * 24 * 60 + minutes(?end-?now) * 60 + seconds(?end-?now) ) ) as ?moveremaining ) \n"
	    + "        }    \n"
	    + "}     ";
    private final String getOutputPorts = "PREFIX pos: <http://example.org/property/position#>"
	    + "PREFIX ex:<http://example.org/>"
	    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + "SELECT ?op ?x ?y\n"
	    + "WHERE{\n"
	    + "    ?op a ex:Port ;  ex:located [ pos:xPos ?x ; pos:yPos ?y ] .\n"
	    + "}";
    private final String getOrders = "PREFIX pos: <http://example.org/property/position#>"
	    + "PREFIX ex:<http://example.org/>"
	    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + "SELECT ?order ?timestamp ?status \n"
	    + "WHERE{\n"
	    + "    ?order a ex:Order ;  ex:created ?timestamp; ex:status ?status .\n"
	    + "} ORDER BY (?timestamp)";
}

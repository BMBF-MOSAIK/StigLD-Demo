/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import java.util.HashMap;
import java.util.Map;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author tospie
 */
public class ScenarioStatistics {

    public int numOrders;
    public int numWorkstations;
    public int numTransporters;
    public Map<String, TransporterStatistics> initialPositions = new HashMap<>();

    public ScenarioStatistics() {
    }

    public void read(String endpoint) {
	readArtifactCounts(endpoint);
	readInitialTransporterPositions(endpoint);
    }

    private void readArtifactCounts(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.count_artifacts)).execSelect();
	while (results.hasNext()) {
	    QuerySolution line = results.next();
	    numOrders = line.getLiteral("orders").getInt();
	    numWorkstations = line.getLiteral("workstations").getInt();
	    numTransporters = line.getLiteral("transporters").getInt();
	}
    }

    private void readInitialTransporterPositions(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.transporter_pos)).execSelect();
	while (results.hasNext()) {
	    QuerySolution line = results.next();
	    String id = line.getResource("t").getLocalName();
	    int x = line.getLiteral("x").getInt();
	    int y = line.getLiteral("y").getInt();
	    initialPositions.put(id, new TransporterStatistics(x, y, 0));
	}
    }
}

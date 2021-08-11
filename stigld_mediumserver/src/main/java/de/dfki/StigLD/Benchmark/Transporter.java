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
public class Transporter {

    public static final Map<String, TransporterStatistics> Statistics = new HashMap<>();

    public static void countTransporterSteps(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.transporter_pos)).execSelect();
	while (results.hasNext()) {
	    updateTransporter(results.next());
	}
    }

    private static void updateTransporter(QuerySolution n) {
	String transporterUri = n.getResource("t").getLocalName();
	int x = n.getLiteral("x").getInt();
	int y = n.getLiteral("y").getInt();
	TransporterStatistics updated = getTransporterUpdate(transporterUri, x, y);
	Statistics.put(transporterUri, updated);
    }

    private static TransporterStatistics getTransporterUpdate(String transporterUri, int x, int y) {
	TransporterStatistics oldForN = Statistics.getOrDefault(transporterUri, new TransporterStatistics(x, y, 0));
	int diffTraveled = Math.abs(oldForN.x - x) + Math.abs(oldForN.y - y);

	// If distance is larger one, we may suspect teleport back to init position
	int newDist = diffTraveled <= 1 ? oldForN.totalDistanceTravelled + diffTraveled : oldForN.totalDistanceTravelled;

	return new TransporterStatistics(x, y, newDist);
    }
}

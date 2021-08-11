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
public class WorkstationLoads {

    public static Map<String, WorkstationStatistics> Statistics = new HashMap<>();

    public static void getWorkstationStatistics(String endpoint) {
	getCurrentTaskLists(endpoint);
	readCurrentLoads(endpoint);
	updateTotalTasks();
    }

    private static void getCurrentTaskLists(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.workstation_tasks)).execSelect();
	while (results.hasNext()) {
	    QuerySolution n = results.next();
	    String wsUri = n.getResource("ws").getLocalName();
	    String taskStartTime = n.getLiteral("startTime").getString();
	    if (!Statistics.containsKey(wsUri)) {
		Statistics.put(wsUri, new WorkstationStatistics());
	    }
	    WorkstationStatistics s = Statistics.get(wsUri);
	    s.putTask(taskStartTime);
	}
    }

    private static void updateTotalTasks() {
	Statistics.values().forEach((ws) -> {
	    ws.totalLoad = ws.TaskCount();
	});
    }

    private static void readCurrentLoads(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.workstation_task_counts)).execSelect();
	while (results.hasNext()) {
	    updateMaxLoads(results.next());
	}
    }

    private static void updateMaxLoads(QuerySolution n) {
	String wsUri = n.getResource("machine").getLocalName();
	int currentLoad = n.getLiteral("n").getInt();
	if (!Statistics.containsKey(wsUri)) {
	    Statistics.put(wsUri, new WorkstationStatistics());
	}
	WorkstationStatistics ws = Statistics.get(wsUri);
	ws.currentLoad = currentLoad;
	ws.maxLoad = currentLoad > ws.maxLoad ? currentLoad : ws.maxLoad;
    }
}

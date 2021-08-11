/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author tospie
 */
public class MakeshiftStatistics {

    public float avg;
    public float median;
    public float min;
    public float max;

    public void read(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.makeshift_times)).execSelect();
	while (results.hasNext()) {
	    QuerySolution next = results.next();
	    avg = next.getLiteral("avg").getFloat();
	    median = next.getLiteral("median").getFloat();
	    min = next.getLiteral("min").getFloat();
	    max = next.getLiteral("max").getFloat();
	}
    }
}

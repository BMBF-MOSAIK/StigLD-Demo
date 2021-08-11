/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;

/**
 *
 * @author tospie
 */
public class Benchmark {

    private int stepCounter = 0;
    String endpoint = "http://localhost:3230/ds/";
    ScenarioStatistics scenarioStatistics = new ScenarioStatistics();
    MakeshiftStatistics makeshiftStatistics = new MakeshiftStatistics();
    ObjectMapper mapper = new ObjectMapper();
    List<Long> queryTimes = new ArrayList<>();
    private boolean initialized = false;

    public Benchmark() {
    }

    public void init() {
	scenarioStatistics.read(endpoint);
	initialized = true;
    }

    public void measure() {
	if (!initialized) {
	    init();
	}
	stepCounter++;
	/// send benchmarking queries
	Transporter.countTransporterSteps(endpoint);
	WorkstationLoads.getWorkstationStatistics(endpoint);

	/// If no more orders are open, write log file
	checkDoneAndFinalize();
    }

    public void lastQueryTime(long execTimeInMs) {
	synchronized(queryTimes){
	    queryTimes.add(execTimeInMs);
	}
    }

    private void checkDoneAndFinalize() {
	try {
	    if (!QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.open_orders)).execAsk()) {
		System.out.println("[StigLD Benchmark] ------ FINISHED! ALL ORDERS PROCESSED ------ ");
		makeshiftStatistics.read(endpoint);
		writeResults();
	    }
	} catch (JsonProcessingException e) {
	    System.out.println("[StigLD Benchmark] Something went wrong when serilazing the benchmark results: " + e.getMessage());
	} catch (FileNotFoundException e) {
	    System.out.println("[StigLD Benchmark] Something went wrong when accessing the file to save the log: " + e.getMessage());
	}

    }

    private void writeResults() throws JsonProcessingException, FileNotFoundException {
	String basicStatistics = mapper.writeValueAsString(scenarioStatistics);
	String transporterStatistics = mapper.writeValueAsString(Transporter.Statistics);
	String workstationStatistics = mapper.writeValueAsString(WorkstationLoads.Statistics);
	String makeshift = mapper.writeValueAsString(makeshiftStatistics);
	synchronized(queryTimes) {
	    Collections.sort(queryTimes);
	}
	PrintWriter printWriter = new PrintWriter("logs/benchmark-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".log");
	printWriter.println("Scenario Setup: ");
	printWriter.println(" ");
	printWriter.println(basicStatistics);
	printWriter.println(" ");
	printWriter.println("Total number of steps: " + stepCounter);
	printWriter.println(" ");
	printWriter.println("Execution Times: ");
	printWriter.println("------------------------------");
	printWriter.println("Min: " + Collections.min(queryTimes));
	printWriter.println("Max: " + Collections.max(queryTimes));
	printWriter.println("Avg: " + queryTimes.stream().mapToDouble(q -> q).average());
	printWriter.println("Median:" + getMedianQueryTime());
	printWriter.println(" ");
	printWriter.println("Transporter Statistics: ");
	printWriter.println(" ");
	printWriter.println(transporterStatistics);
	printWriter.println(" ");
	printWriter.println("Workstation Statistics: ");
	printWriter.println(" ");
	printWriter.println(workstationStatistics);
	printWriter.println("Makeshift Times: ");
	printWriter.println(" ");
	printWriter.println(makeshift);
	printWriter.close();
    }

    private long getMedianQueryTime() {
	long median;
	if (queryTimes.size() % 2 == 0) {
	    median = ((long) queryTimes.get(queryTimes.size() / 2) + (long) queryTimes.get(queryTimes.size() / 2 - 1)) / 2;
	} else {
	    median = (long) queryTimes.get(queryTimes.size() / 2);
	}

	return median;
    }
}

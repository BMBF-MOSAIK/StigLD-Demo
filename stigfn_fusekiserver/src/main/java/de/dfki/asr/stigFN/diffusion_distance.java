package de.dfki.asr.stigFN;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase5;

public class diffusion_distance extends FunctionBase5 {

    @Override
    public NodeValue exec(NodeValue sourceId, NodeValue distance, NodeValue duration, NodeValue concentration, NodeValue rate) {

	try {
	    Concentration conc = ConcentrationsManager.getAndAddIfNotExist(sourceId.asString());
	    double output = conc.getAtDistance(distance, duration, concentration, rate);
	    return NodeValue.makeDouble(output);
	} catch (Exception e) {
	    System.out.println("[diffusion_distance] An exception occured during diffusion_distance.exec: " + e.getMessage());
	}
	return NodeValue.makeDouble(0.0);
    }
}

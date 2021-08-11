/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.asr.stigFN;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.sparql.expr.NodeValue;

/**
 *
 * @author tospie
 */
public class Concentration {

    // diffusion parameters
    @Getter
    private final double L = 20;

    @Getter
    private final double nx = L * 10;

    @Getter
    @Setter
    private boolean calculating = false;

    @Getter
    @Setter
    private double values[];

    @Getter
    public final Object calcMutex = new Object();

    @Getter
    public final Object valueMutex = new Object();

    public Concentration() {
	values = new double[(int) (nx+1)];
	for (int i = 0; i < values.length; i++) {
	    values[i] = 0;
	}
    }

    public double getAtDistance(NodeValue distance, NodeValue duration, NodeValue concentration, NodeValue rate) {
	double dist = distance.getDouble();
	// lock calculating

	synchronized (calcMutex) {
	    if (!calculating) {
		calculating = true;
		// start new Thread
		Calculate runnable = new Calculate(duration, concentration, rate, this);
		new Thread(runnable).start();
	    }
	}
	// lock values
	synchronized (valueMutex) {
	    double result = dist < L / 2 ? values[(int) (((L / 2) - dist) * (nx / L))] : 0;
	    return result;
	}
    }
}

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
import org.apache.jena.sparql.expr.NodeValue;

/**
 *
 * @author tospie
 */
public class Calculate implements Runnable {

    NodeValue duration;
    NodeValue concentration;
    NodeValue rate;

    Concentration caller;

    public Calculate(NodeValue duration, NodeValue concentration, NodeValue rate, Concentration caller) {
	this.duration = duration;
	this.concentration = concentration;
	this.rate = rate;
	this.caller = caller;
    }

    @Override
    public void run() {
	calculate();
    }

    private void calculate() {
	double T = duration.getDouble();            // can be taken from input of sparql function (duration)
	double nt = T * 10;
	double alpha = rate.getDouble();            // taken from sparql function (rate)
	double conc = concentration.getDouble();    // initial concentration taken from sparql function (concentration)

	double[] x = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 3.0, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 5.0, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 6.0, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 7.0, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 8.0, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9, 9.0, 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9, 10.0, 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9, 11.0, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8, 11.9, 12.0, 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9, 13.0, 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 14.0, 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8, 14.9, 15.0, 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.9, 16.0, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8, 16.9, 17.0, 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7, 17.8, 17.9, 18.0, 18.1, 18.2, 18.3, 18.4, 18.5, 18.6, 18.7, 18.8, 18.9, 19.0, 19.1, 19.2, 19.3, 19.4, 19.5, 19.6, 19.7, 19.8, 19.9, 20.0};
	double dx = x[1] - x[0];
	double[] t = new double[(int) nt + 1];
	linspace(t, 0, T, T / nt);
	double dt = t[1] - t[0];
	double F = alpha * dt / (dx * dx);
//        System.out.println("T " + T+" -nt "+nt+" -alpha "+alpha+" -dx "+dx+" -dt "+dt+" -tsize "+t.length+" -xsize "+x.length+" -F "+F);
//        System.out.println("F IS " + F+ "\n-------------");
	double fac = 1.0 - 2.0 * F;
	double[] init_conc = new double[(int) caller.getNx() + 1];
	boolean req_plot = false;
	for (int i = 0; i < init_conc.length; i++) //calculating the initial function
	{
	    if (x[i] >= ((caller.getL() / 2) - 0.5) && x[i] <= ((caller.getL() / 2) + 0.5)) {
		init_conc[i] = conc * Math.sin(2 * Math.PI * 5 * (x[i] - ((caller.getL() / 2) - 0.5)) / caller.getL());
	    } else {
		init_conc[i] = 0;
	    }
	}

	List<Double> xList = new ArrayList<>();  //converting to lists, because the "plot" python package requires this
	for (double n : x) {
//            System.out.println(n);
	    xList.add(n);
	}
//        System.out.println("List: "+xList);
	List<Double> y1 = new ArrayList<>();     //converting to lists, because the "plot" python package requires this
	for (double n : init_conc) {
//            System.out.println(im+"--->"+n);
	    y1.add(n);
	}

	double[] conc_old = init_conc;
	double[] conc_new = new double[(int) caller.getNx() + 1];

	for (int j = 0; j < nt; j++) //iteratively calculating the diffusion curve
	{
	    for (int i = 1; i < conc_new.length - 1; i++) {
		conc_new[i] = round(fac * conc_old[i] + F * (conc_old[i - 1] + conc_old[i + 1]), 8);
	    }
	    conc_old = conc_new;
	}

//        System.out.println("Final concentration at distance "+distance+" from point of pheromone deposition after "+T+" seconds is "+conc_new[(int)(((L/2)-dist)*(nx/L))]);	
	if (req_plot) {      //option to plot the result if necessary using pyhton "plot" package
	    List<Double> y = new ArrayList<>();     //as above, required for the "plot" python package
	    for (double n : conc_new) {
		y.add(n);
	    }

	    Plot plt = Plot.create();
	    plt.plot()
		    .add(xList, y)
		    .label("Diffused profile")
		    .linestyle("-");
	    plt.plot()
		    .add(xList, y1)
		    .label("Initial pheromone deposit")
		    .linestyle("-");
	    plt.xlabel("space");
	    plt.ylabel("intensity");
	    plt.xlim(0, 21);
	    plt.title("Diffused curve after " + T + " seconds");
	    plt.legend();
	    try {
		plt.show();
	    } catch (IOException | PythonExecutionException e) {
		e.printStackTrace();
	    }
	}

	// Lock object in thread
	synchronized (caller.getValueMutex()) {
	    caller.setValues(conc_new);
	}
	synchronized (caller.getCalcMutex()) {
	    caller.setCalculating(false);
	}
    }

    private static double round(double value, int precision) {
	int scale = (int) Math.pow(10, precision);
	return (double) Math.round(value * scale) / scale;
    }

    private static void linspace(double[] array, double start, double end, double space) {
	array[0] = start;
	for (int i = 1; i < array.length; i++) {
	    array[i] = round(array[i - 1] + space, 2);
	}
    }
}

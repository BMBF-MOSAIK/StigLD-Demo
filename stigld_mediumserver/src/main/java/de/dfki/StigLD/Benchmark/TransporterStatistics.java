/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

/**
 *
 * @author tospie
 */
public class TransporterStatistics {

    public int x;
    public int y;
    public int totalDistanceTravelled;

    public TransporterStatistics(int x, int y, int d) {
	this.x = x;
	this.y = y;
	this.totalDistanceTravelled = d;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.asr.stigFN;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tospie
 */
public class ConcentrationsManager {

    private static volatile Map<String, Concentration> concentrations = new HashMap<>();

    public static Concentration getAndAddIfNotExist(String sourceId) {
	if (!concentrations.containsKey(sourceId)) {
	    System.out.println("[diffusion_distance] Source Stigma with ID " + sourceId + " not yet present. Adding.");
	    concentrations.put(sourceId, new Concentration());
	}

	System.out.println("[diffusion_distance] Returning Source Stigma with ID " + sourceId);
	return concentrations.get(sourceId);
    }
}

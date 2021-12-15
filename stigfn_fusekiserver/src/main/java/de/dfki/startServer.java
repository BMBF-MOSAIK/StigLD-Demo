package de.dfki;

import de.dfki.asr.stigFN.*;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.function.FunctionRegistry;


import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class startServer {
    public static void main(String[] args) throws FileNotFoundException {
        FusekiLogging.setLogging();

        FunctionRegistry ref = FunctionRegistry.get();
        ref.put("http://www.dfki.de/func#dist_manhattan", manhattanDistance.class);
        ref.put("http://www.dfki.de/func#duration_secs", duration_sec.class);
        ref.put("http://www.dfki.de/func#diffusion_1D", diffusion_distance.class);
        ref.put("http://www.dfki.de/func#duration_msecs", duration_msec.class);
        ref.put("http://www.dfki.de/func#linear_decay", linearDecay.class);
        ref.put("http://www.dfki.de/func#exponential_decay", exponentialDecay.class);

        Model model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream("../onlineDemo_TestModel.ttl"), null, "TTL");
        Dataset ds = DatasetFactory.create(model);
        FusekiServer server = FusekiServer.create()
                .port(3230)
                .add("/ds", ds)
                .build();
        server.start();

    }
}

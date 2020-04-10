package edu.lvc.cds;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.*;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/BeerRating/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in edu.lvc.cds package
        final ResourceConfig rc = new ResourceConfig().packages("edu.lvc.cds");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        storeBeers(BeerResource.beersDB);
        server.stop();
    }
    private static void storeBeers(ConcurrentHashMap<Integer,Beer> database) throws IOException {
        File file = new File("beers.txt");
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(database);
        os.close();

    }


    public static ConcurrentHashMap loadbeers() throws IOException, ClassNotFoundException {
        File file = new File("beers.txt");
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream inS = new ObjectInputStream(in);
        ConcurrentHashMap<Integer,Beer> database = (ConcurrentHashMap<Integer, Beer>) inS.readObject();
        inS.close();
        return database;
    }

}


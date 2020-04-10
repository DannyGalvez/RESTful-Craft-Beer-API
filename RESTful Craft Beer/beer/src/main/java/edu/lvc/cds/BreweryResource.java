package edu.lvc.cds;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Specifies the resource class for beers on an endpoint /beers, initializes a beer database.
// The beer database is initialized as a text file in the local files.
@Path("/breweries")
public class BreweryResource {


        private ConcurrentHashMap<Integer, Brewery> breweriesDB;
        private ConcurrentHashMap<Integer, Beer> beersDB;




        public static AtomicInteger idNum = new AtomicInteger();


    // Get method for the endpoint /breweries.
    // pre: A database of breweries exists on the correct endpoint
    // post: A JSON representation of the database of all breweries is returned to the client
    //       containing an array of objects containing their instance data.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDB() throws IOException, ClassNotFoundException {
        BeerResource re = new BeerResource();
        JSONArray brewskis = new JSONArray();
        for (ConcurrentHashMap.Entry<Integer,Brewery> entry : breweriesDB.entrySet()){
            brewskis.add(re.outputGetBrewery(breweriesDB.get(entry.getKey())));
        }

        return brewskis.toJSONString();
    }

    public int getID(){
        return idNum.incrementAndGet();
    }

}

package edu.lvc.cds;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/beers")
public class BeerResource {

    public static ConcurrentHashMap<Integer, Beer> beersDB = new ConcurrentHashMap<Integer, Beer>();

    private AtomicInteger idNum = new AtomicInteger();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createBeer(String is) throws IOException, ParseException {


       Beer beer = readBeer(is);
       beer.setID(1);
       beersDB.put(1, beer);

        System.out.println(beersDB.get(1).getName());
        System.out.println(beersDB.get(1).getABV());
        System.out.println(beersDB.get(1).getID());
        System.out.println(beersDB.get(1).getIbu());
        System.out.println(beersDB.get(1).getBrewery());
        System.out.println(beersDB.get(1).getRating());
        System.out.println(beersDB.get(1).getDesc());




    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDB() throws IOException, ClassNotFoundException {
        beersDB = Main.loadbeers();
        JSONArray brewskis = new JSONArray();

        brewskis.add(outputBeer(beersDB.get(1)));
        return brewskis.toJSONString();
    }

    @GET
    @Path("{id}")
    @Produces("application/JSON")
    public StreamingOutput getBeer(@PathParam("id") int id){
        final Beer beer = beersDB.get(id);

        if (beer == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                outputBeer(beer);
            }
        };
    }

    @POST
    @Path("/{id}/reviews")
    @Produces("application/JSON")
    public Response rateBeer(InputStream is, @PathParam("id") int id) throws IOException, ParseException {
        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject)parse.parse(new InputStreamReader(is));
        int rating = (Integer)obj.get("Rating");
        Beer beer = beersDB.get(id);
        beer.insertRating(rating);
        System.out.println("Successfully created rating for beer " + beer.getID());
        return Response.created(URI.create("/beers" + beer.getID() + "/reviews")).build();
    }

    @GET
    @Path("/{id}/reviews")
    @Produces("application/JSON")
    public StreamingOutput getRatings(@PathParam("id") int id){
        final Beer beer = beersDB.get(id);
        if (beer == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                outputRatings(outputStream, beer);
            }
        };
    }

    protected void outputRatings(OutputStream os, Beer beer){
        JSONArray ratings = new JSONArray();
        JSONObject rate = new JSONObject();
        ArrayList<Integer> li = beer.getAllRatings();

        for (int i = 0; i < li.size(); i++) {
            ratings.add(li.get(i));
        }
        rate.put("Ratings", ratings);
        PrintStream writer = new PrintStream(os);
        writer.println(rate);

    }


    protected JSONObject outputBeer(Beer beer){

        JSONObject obj = new JSONObject();
        obj.put("Name", beer.getName());
        obj.put("Brewery", beer.getBrewery());
        obj.put("IBU", beer.getIbu());
        obj.put("ABV", beer.getABV());
        obj.put("Rating", beer.getRating());
        obj.put("Description", beer.getDesc());

        return obj;

    }

    private Beer readBeer(String is) throws IOException, ParseException {


        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject)parse.parse(is);

        Beer beer = new Beer();
        beer.setName((String)obj.get("Name"));
        beer.setBrewery((String)obj.get("Brewery"));
        beer.setIbu((String) obj.get("IBU"));
        beer.setABV((String) obj.get("ABV"));
        beer.setRating((String) obj.get("Rating"));
        beer.setDesc((String) obj.get("Description"));

        return beer;
    }


}


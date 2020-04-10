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
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Specifies the resource class for beers on an endpoint /beers, initializes a beer database.
// The beer database is initialized as a text file in the local files.
@Path("/beers")
public class BeerResource {

    private ConcurrentHashMap<Integer, Beer> beersDB;
    private ConcurrentHashMap<Integer, Brewery> breweriesDB;

    {
        try {
            BreweryResource re = new BreweryResource();
            beersDB = loadbeers();
            breweriesDB = loadbrewery();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private static AtomicInteger idNum = new AtomicInteger();


    // Post method on the /beers endpoint. Takes in a string representation in JSON and parses
    // the string, assigning variables to a new beer object.
    // pre: JSON is formatted correctly and the objects are written with correct keys
    // post: A new beer is created on the server
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createBeer(String is) throws IOException, ParseException, ClassNotFoundException {
        boolean clone = false;

       Beer beer = readBeer(is, false);

       BreweryResource re = new BreweryResource();
        for (ConcurrentHashMap.Entry<Integer,Beer> entry : beersDB.entrySet()) {
            String name = beersDB.get(entry.getKey()).getName();
            String brewery = beersDB.get(entry.getKey()).getBrewery();

            if (beer.getName().equals(name) && beer.getBrewery().equals(brewery)) {
                clone = true;
                break;
            }
        }
        if (clone == false){
            beer.setID(idNum.incrementAndGet());
            beer.setURL();
            beersDB.put(beer.getID(), beer);
        }
         boolean found = false;
        for (ConcurrentHashMap.Entry<Integer,Brewery> entry : breweriesDB.entrySet()) {
            if (beer.getBrewery().equals(entry.getValue().getName())){
                found = true;
                Brewery brewery = entry.getValue();
                brewery.insertBeer(beer);
                breweriesDB.put(entry.getValue().getID(), brewery);
            }
            else if (!found){
                Brewery brewery = new Brewery();
                brewery.setID(re.getID());
                brewery.setName(beer.getBrewery());
                brewery.insertBeer(beer);
                breweriesDB.put(brewery.getID(), brewery);
            }
        }


            storeBrewery(breweriesDB);
            storeBeers((beersDB));

    }

    // Get method for the endpoint /beers.
    // pre: A database of beers exists on the correct endpoint
    // post: A JSON representation of the database of all beers is returned to the client
    //       containing an array of objects containing their instance data.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDB() throws IOException, ClassNotFoundException {
        //beersDB = loadbeers();
        JSONArray brewskis = new JSONArray();
        for (ConcurrentHashMap.Entry<Integer,Beer> entry : beersDB.entrySet()){
            brewskis.add(outputBeer(beersDB.get(entry.getKey()), false));
        }

        return brewskis.toJSONString();
    }

    // Get method for the endpoint /beers/{id}.
    // pre: A beer exists in the database with correct ID number
    // post: A JSON representation of a beer object corresponding to the ID number is returned,
    //       containing that specific beer's instance data.
    @GET
    @Path("{id}")
    @Produces("application/JSON")
    public String getBeer(@PathParam("id") int id){
        final Beer beer = beersDB.get(id);

        if (beer == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return outputBeer(beer, true).toJSONString();
    }

    // Post method on the /beers/{id}/reviews endpoint. Takes in a JSON string representation
    // and parses the string, assigning the rating of the beer to a beer specified by the ID number
    // in the URL path.
    // pre: JSON is formatted correctly and the objects are written with correct keys
    // post: A beer rating is added to the beer's review history, and a new overall beer rating
    //       is calculated.
    @POST
    @Path("{id}/reviews")
    @Produces("application/JSON")
    public void rateBeer(String is, @PathParam("id") int id) throws IOException, ParseException {
        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject) parse.parse(is);
        Beer beer = beersDB.get(id);
        beer.printRatings();
        String rating = (String) obj.get("Rating");

        if (Double.parseDouble(rating) < 5.1) {
            beer.insertRating(rating);
            beersDB.replace(beer.getID(), beer);
            beersDB.get(beer.getID()).printRatings();
            storeBeers((beersDB));


        }

        else{
            System.out.println("Not a valid rating. Must be between 0-5");
        }

    }

    // Get method for the endpoint /beers/{id}/reviews.
    // pre: A beer exists on the correct endpoint and it's review history is non null.
    // post: A JSON representation of the review history of a beer is presented along with
    //       that beer's name, and overall rating.
    @GET
    @Path("/{id}/reviews")
    @Produces("application/JSON")
    public String getRatings(@PathParam("id") int id){
        final Beer beer = beersDB.get(id);
        if (beer == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return outputRatings(beer).toJSONString();
    }

    // Takes in a beer object and returns a JSON representation of that beer's
    // review history along with links and the beer's name and overall rating.
    // pre: Beer is non null
    // post: A JSON object containing the beer's rating and data is returned
    private JSONObject outputRatings(Beer beer){
        JSONArray ratings = new JSONArray();
        JSONObject rate = new JSONObject();
        JSONObject rel = new JSONObject();
        JSONObject rel1 = new JSONObject();
        JSONArray links = new JSONArray();
        rel.put("rel", "previous");
        rel.put("href", beer.getURL() + beer.getID());
        rel1.put("rel", "self");
        rel1.put("href", beer.getURL() + beer.getID() + "/reviews");
        links.add(rel);
        ArrayList<Double> li = beer.getAllRatings();

        for (int i = 0; i < li.size(); i++) {
            System.out.println(li.get(i));
            ratings.add(li.get(i));
        }
        rate.put("Name", beer.getName());
        rate.put("Current Rating:", beer.getRating());
        rate.put("Previous Ratings", ratings);
        rate.put("links", links);
        return rate;

    }


    // Takes in a beer and a boolean value. The function parses the beer instance
    // data and outputs the info as a JSON object. The boolean value is determined
    // by what function is making the call, so that the method knows what links
    // to add for specific rel attributes.
    // pre: Beer is formatted correctly and non null
    // post: A JSON object containing all the beer data is returned
    private JSONObject outputBeer(Beer beer, boolean rel){

        JSONArray re = new JSONArray();
        JSONObject obj = new JSONObject();

        ArrayList<Double> ra = beer.getAllRatings();
        JSONArray rates = new JSONArray();
        for (int i = 0; i < ra.size(); i++) {
            rates.add(ra.get(i));
        }
        obj.put("Name", beer.getName());
        obj.put("Brewery", beer.getBrewery());
        obj.put("IBU", beer.getIbu());
        obj.put("ABV", beer.getABV());
        obj.put("Rating", beer.getRating());
        obj.put("Description", beer.getDesc());
        obj.put("ID", beer.getID());
        obj.put("Previous Ratings", rates);


        // if endpoint is /beers/ID
        if (rel) {
            JSONObject temp1 = new JSONObject();
            JSONObject temp2 = new JSONObject();
            JSONObject temp3 = new JSONObject();
            temp1.put("rel", "next");
            temp1.put("href", beer.getURL() + beer.getID() + "/reviews");
            temp2.put("rel", "previous");
            temp2.put("href", beer.getURL());
            temp3.put("rel", "self");
            temp3.put("href", beer.getURL() + beer.getID());
            re.add(temp1);
            re.add(temp2);
            re.add(temp3);

            obj.put("links", re);
        }

        // if endpoing is /beers/ID/reviews
        else if (!rel){
            JSONObject obj1 = new JSONObject();
            obj1.put("rel", "next");
            obj1.put("href", beer.getURL() + beer.getID());
            re.add(obj1);
            obj.put("links", re);
        }


        return obj;

    }

    // Helper method for parsing JSON. Takes in a JSON representation of a beer and parses
    // the JSON, assigning variables from the string to a new beer object. Also takes in a
    // boolean value saying whether or not the beer already exists, so the correct links
    // corresponding to rel values can be added.
    // pre: JSON is formatted correctly and variable keys are correct
    // post: A beer object is returned containing the data in the JSON string
    public Beer readBeer(String is, boolean created) throws IOException, ParseException {


        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject)parse.parse(is);



        Beer beer = new Beer();

        System.out.println(obj.toJSONString());

        beer.setName((String)obj.get("Name"));
        beer.setBrewery((String)obj.get("Brewery"));
        beer.setIbu((String) obj.get("IBU"));
        beer.setABV((String) obj.get("ABV"));
        beer.setDesc((String) obj.get("Description"));
        beer.setRating((String) obj.get("Rating"));

        beer.setURL();
        if (created){
            beer.setID(Integer.parseInt(obj.get("ID").toString()));

            JSONArray rate = (JSONArray) obj.get("Previous Ratings");

            ArrayList<Double> ratings = new ArrayList<>(1);
            for (int i = 0; i < rate.size(); i++) {
                ratings.add((Double)rate.get(i));

            }
            beer.setAllRatings(ratings);
        }


        return beer;
    }


    // Takes in a concurrent hashmap and stores the map as JSON on a local text file.
    // pre: Hashmap is non null
    // post: A JSON representation of the hashmap is stored on a text file
    private void storeBeers(ConcurrentHashMap<Integer,Beer> database) throws IOException {
        JSONArray data = new JSONArray();
        File file = new File("beers.txt");
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(out);
        for (ConcurrentHashMap.Entry<Integer,Beer> entry : database.entrySet()) {
            data.add(database.get(entry.getKey()).getID(), outputBeer(database.get(entry.getKey()), false));
        }
        os.writeObject(data);
        os.close();


    }

    // Reads a text file containing a JSON representation of a hashmap, parses the data and
    // creates a new hashmap with the parsed JSON data. The new hashmap is returned.
    // pre: The text file exists and contains correctly formatted JSON data of a Hashmap
    // post: A Hashmap of data from the text file is returned
    public  ConcurrentHashMap loadbeers() throws IOException, ClassNotFoundException, ParseException {
        File file = new File("beers.txt");
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream inS = new ObjectInputStream(in);
        ConcurrentHashMap<Integer,Beer> database = new ConcurrentHashMap<>();
        JSONArray db = (JSONArray)inS.readObject();


        for (int i = 0; i < db.size(); i++) {
            JSONObject jo = (JSONObject) db.get(i);
            Beer beer = readBeer(jo.toJSONString(), true);

            database.put(beer.getID(), beer);

        }

        inS.close();

        return database;
    }

    // Get method for the database of beers
    // pre: Beers database is non null
    // post: Returns the database of beers as a Concurrent Hashmap
    public ConcurrentHashMap<Integer, Beer> getBeersDB() {
        return beersDB;
    }

    public JSONObject outputGetBrewery(Brewery brewery) {

        JSONObject obj = new JSONObject();
        JSONArray links = new JSONArray();
        JSONObject obj3 = new JSONObject();
        JSONObject obj4 = new JSONObject();
        JSONArray beers = new JSONArray();
        ArrayList<Beer> b = brewery.getBeers();

        obj.put("Brewery", brewery.getName());
        obj.put("ID", brewery.getID());

        for (int i = 0; i < b.size(); i++) {
            Beer temp = b.get(i);
            obj3.put("Beer Name", temp.getName());
            obj3.put("href", temp.getURL());
            beers.add(obj3);
        }
        obj.put("Beers", beers);

        obj4.put("rel", "self");
        obj4.put("href", brewery.getURL());
        links.add(obj4);
        obj.put("links", obj4);
        return obj;
    }


    public JSONObject outputBrewery(Brewery brewery){

        JSONObject obj = new JSONObject();
        JSONArray links = new JSONArray();
        JSONObject obj3 = new JSONObject();
        JSONArray beers = new JSONArray();
        ArrayList<Beer> b = brewery.getBeers();

        obj.put("Brewery", brewery.getName());
        obj.put("ID", brewery.getID());


        for (int i = 0; i < b.size(); i++) {
            Beer temp = b.get(i);

            beers.add(outputBeer(temp, false));
        }
        obj.put("Beers", beers);

        obj3.put("rel", "self");
        obj3.put("href", brewery.getURL());
        links.add(obj3);
        obj.put("links", obj3);

        return obj;

    }

    // Takes in a concurrent hashmap and stores the map as JSON on a local text file.
    // pre: Hashmap is non null
    // post: A JSON representation of the hashmap is stored on a text file
    public void storeBrewery(ConcurrentHashMap<Integer,Brewery> database) throws IOException {
        JSONArray data = new JSONArray();
        File file = new File("breweries.txt");
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(out);
        for (ConcurrentHashMap.Entry<Integer,Brewery> entry : database.entrySet()) {
            System.out.println(database.get(entry.getKey()).getID());
            System.out.println(outputBrewery(database.get(entry.getKey())).toJSONString());
            data.add(database.get(entry.getKey()).getID(), outputBrewery(database.get(entry.getKey())));
        }
        os.writeObject(data);
        os.close();


    }

    // Reads a text file containing a JSON representation of a hashmap, parses the data and
    // creates a new hashmap with the parsed JSON data. The new hashmap is returned.
    // pre: The text file exists and contains correctly formatted JSON data of a Hashmap
    // post: A Hashmap of data from the text file is returned
    public  ConcurrentHashMap loadbrewery() throws IOException, ClassNotFoundException, ParseException {
        File file = new File("breweries.txt");
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream inS = new ObjectInputStream(in);
        ConcurrentHashMap<Integer,Brewery> database = new ConcurrentHashMap<>();
        JSONArray db = (JSONArray)inS.readObject();


        for (int i = 0; i < db.size(); i++) {
            JSONObject jo = (JSONObject) db.get(i);
            Brewery brewery = readBrewery(jo.toJSONString());
            database.put(brewery.getID(), brewery);

        }

        inS.close();

        return database;
    }

    private Brewery readBrewery(String is) throws ParseException, IOException {
        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject)parse.parse(is);

        Brewery brewery = new Brewery();

        ArrayList<Beer> li = new ArrayList<>(2);
        JSONArray beers = (JSONArray)obj.get("Beers");
        for (int i = 0; i < beers.size(); i++) {
            JSONObject jo = (JSONObject)beers.get(i);
            li.add(readBeer(jo.toJSONString(), true));
        }

        brewery.setID(Integer.parseInt(obj.get("ID").toString()));
        brewery.setName((String) obj.get("Brewery"));
        brewery.setBeers(li);

        return brewery;
    }


}


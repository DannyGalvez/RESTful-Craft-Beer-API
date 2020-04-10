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
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Specifies the resource class for users on an endpoint /users, initializes a user database.
// The user database is initialized as a text file in the local files.
@Path("/users")
public class UserResource {



        public ConcurrentHashMap<Integer, User> usersDB;

    {
        try {
            usersDB = loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    static AtomicInteger idNum = new AtomicInteger();

    // Post method on the /users endpoint. Takes in a string representation in JSON and parses
    // the string, assigning variables to a new user object.
    // pre: JSON is formatted correctly and the objects are written with correct keys
    // post: A new user is created on the server
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUser(String is) throws IOException, ParseException {
        boolean clone = false;

        User user = readUser(is, false);


        for (ConcurrentHashMap.Entry<Integer,User> entry : usersDB.entrySet()) {
            String name = usersDB.get(entry.getKey()).getUsername();

            if (user.getUsername().equals(name)) {
                clone = true;
                break;
            }
        }
        if (clone == false){
            user.setID(idNum.incrementAndGet());
            user.setURL();
            usersDB.put(user.getID(), user);
        }
        System.out.println(user.getURL()+"   " + user.getUsername());
        storeUsers(usersDB);


    }

    // Get method for the endpoint /users.
    // pre: A database of users exists on the correct endpoint
    // post: A JSON representation of the database of all users is returned to the client
    //       containing an array of objects containing their username, links, and the ID
    //       number of their favorite beers.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDB() throws IOException, ClassNotFoundException {

        JSONArray users = new JSONArray();
        for (ConcurrentHashMap.Entry<Integer,User> entry : usersDB.entrySet()){
            users.add(outputUser(usersDB.get(entry.getKey()), false));
        }

        return users.toJSONString();
    }

    // Get method for the endpoint /users/{id}.
    // pre: A user exists in the database with correct ID number
    // post: A JSON representation of a user object corresponding to the ID number is returned,
    //       containing their username, links, and the ID number of their favorite beers.
    @GET
    @Path("{id}")
    @Produces("application/JSON")
    public String getUser(@PathParam("id") int id){
        final User user = usersDB.get(id);

        if (user == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return outputUser(user, true).toJSONString();
    }

    // Post method on the /users/{id}/favorites endpoint. Takes in a JSON string representation
    // and parses the string, assigning the favorite beer to a user specified by the ID number
    // in the URL path.
    // pre: JSON is formatted correctly and the objects are written with correct keys
    // post: A favorite beer is added to a user's profile.
    @POST
    @Path("{id}/favorites")
    @Produces(MediaType.APPLICATION_JSON)
    public void inputFavorite(String is, @PathParam("id") int id) throws ParseException, IOException, ClassNotFoundException {
        JSONObject obj = new JSONObject();
        JSONParser parse = new JSONParser();
        obj = (JSONObject)parse.parse(is);

        User user = usersDB.get(id);

        if (!user.getFavorites().contains((String) obj.get("Beer ID"))) {
            user.addFavorite((String) obj.get("Beer ID"));

        }

        usersDB.replace(user.getID(), user);
        System.out.println(user.getUsername());
        System.out.println(user.getFavoritesbyName().get(0));

        storeUsers(usersDB);



    }

    // Get method for the endpoint /users/{id}/favorites.
    // pre: A database of users exists on the correct endpoint
    // post: A JSON representation of the favorites of the user is returned to the client
    //       containing an array of objects containing their username, links, and their
    //       favorite beers.
    @GET
    @Path("{id}/favorites")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFavorites(@PathParam("id") int id) throws IOException, ClassNotFoundException {
        BeerResource ab = new BeerResource();
        ConcurrentHashMap<Integer, Beer> db = new ConcurrentHashMap<>();
        db = ab.getBeersDB();

        JSONArray data = new JSONArray();
        JSONObject obj = new JSONObject();

        User user = usersDB.get(id);
        ArrayList<String> favs = user.getFavorites();


        JSONObject rel = new JSONObject();
        JSONObject rel2 = new JSONObject();
        JSONArray links = new JSONArray();
        obj.put("User", user.getUsername());
        rel.put("rel", "previous");
        rel.put("href", user.getURL() + user.getID());
        rel2.put("rel", "self");
        rel2.put("href",user.getURL() + user.getID() + "/favorites" );
        links.add(rel);
        links.add(rel2);
        obj.put("links", links);

        for (int i = 0; i < favs.size(); i++) {
            int ID = Integer.parseInt(favs.get(i));
            System.out.println(favs.get(i) + "pppp");

            if(db.containsKey(ID)){

                JSONObject temp = new JSONObject();
                JSONObject rel3 = new JSONObject();
                rel3.put("href", db.get(ID).getURL() + ID);
                temp.put(db.get(ID).getName(), rel3);
                data.add(temp);
            }

            else{
                String error = "No such beer found in database. Go to " + Main.BASE_URI + "beers/ to add a new beer.";
                JSONObject er = new JSONObject();
                er.put("Error", error);
                return er.toJSONString();
            }
        }
        System.out.println(data.toJSONString());

        obj.put("Favorites", data);
        return obj.toJSONString();


    }

    // Helper method for parsing JSON. Takes in a JSON representation of a user and parses
    // the JSON, assigning variables from the string to a new user object. Also takes in a
    // boolean value saying whether or not the beer already exists, so the correct links
    // corresponding to rel values can be added.
    // pre: JSON is formatted correctly and variable keys are correct
    // post: A user object is returned containing the data in the JSON string
    private User readUser(String is, boolean created) throws IOException, ParseException {

        ArrayList<String> favs = new ArrayList<>(2);

        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject)parse.parse(is);

        User user = new User();
        user.setUsername((String)obj.get("Username"));
        JSONArray ar = (JSONArray) obj.get("Favorites");
        for (int i = 0; i < ar.size(); i++) {
            favs.add((String) ar.get(i));
        }
        user.setFavorites(favs);
        if (created){
            user.setID(Integer.parseInt(obj.get("ID").toString()));
        }
        user.setURL();

        return user;
    }

    // Takes in a user, accesses the users favorite beers and adds them to a JSON
    // array and returns the JSON.
    // pre: User exists and their favorites is non-null
    // post: A JSON Array containing their favorites is returned
    private JSONArray filterFav(User user){
        ArrayList<String> orig = user.getFavorites();
        JSONArray favs = new JSONArray();
        for (int i = 0; i < orig.size(); i++) {
            favs.add(orig.get(i));
        }
        return favs;
    }

    // Takes in a user and a boolean value. The function parses the user instance
    // data and outputs the info as a JSON object. The boolean value is determined
    // by what function is making the call, so that the method knows what links
    // to add for specific rel attributes.
    // pre: User is formatted correctly and non null
    // post: A JSON object containing all the user data is returned
    public JSONObject outputUser(User user, boolean rel){

        JSONArray re = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("Username", user.getUsername());
        obj.put("Favorites", filterFav(user));
        obj.put("ID", user.getID());

        // if endpoint is /users/ID
        if (rel) {
            JSONObject temp1 = new JSONObject();
            JSONObject temp2 = new JSONObject();
            JSONObject temp3 = new JSONObject();
            temp1.put("rel", "next");
            temp1.put("href", user.getURL() + user.getID() + "/reviews");
            temp2.put("rel", "previous");
            temp2.put("href", user.getURL());
            temp3.put("rel", "self");
            temp3.put("href", user.getURL() + user.getID());
            re.add(temp1);
            re.add(temp2);
            re.add(temp3);

            obj.put("links", re);
        }

        // if endpoing is /users/ID/favorites
        else if (!rel){
            JSONObject obj1 = new JSONObject();
            obj1.put("rel", "next");
            obj1.put("href", user.getURL() + user.getID());
            re.add(obj1);
            obj.put("links", re);
        }

        return obj;

    }

    // Takes in a concurrent hashmap and stores the map as JSON on a local text file.
    // pre: Hashmap is non null
    // post: A JSON representation of the hashmap is stored on a text file
    public void storeUsers(ConcurrentHashMap<Integer,User> database) throws IOException {
        JSONArray data = new JSONArray();
        File file = new File("users.txt");
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(out);
        for (ConcurrentHashMap.Entry<Integer,User> entry : database.entrySet()) {
            data.add(outputUser(database.get(entry.getKey()), false));

        }
        os.writeObject(data);
        os.close();


    }

    // Reads a text file containing a JSON representation of a hashmap, parses the data and
    // creates a new hashmap with the parsed JSON data. The new hashmap is returned.
    // pre: The text file exists and contains correctly formatted JSON data of a Hashmap
    // post: A Hashmap of data from the text file is returned
    public  ConcurrentHashMap loadUsers() throws IOException, ClassNotFoundException, ParseException {
        File file = new File("users.txt");
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream inS = new ObjectInputStream(in);
        ConcurrentHashMap<Integer,User> database = new ConcurrentHashMap<>();
        JSONArray db = (JSONArray)inS.readObject();


        for (int i = 0; i < db.size(); i++) {
            JSONObject jo = (JSONObject) db.get(i);
            User user = readUser(jo.toJSONString(), true);

            database.put(user.getID(), user);

        }

        inS.close();


        return database;
    }



    }


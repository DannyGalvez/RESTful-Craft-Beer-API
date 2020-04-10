package edu.lvc.cds;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Path("/users")
public class UserResource {

        private Map<Integer, User> usersDB = new ConcurrentHashMap<Integer, User>();

        private AtomicInteger idNum = new AtomicInteger();

        @POST
        @Consumes("application/JSON")
        public Response createUser(InputStream is) throws IOException, ParseException {

            User user = readUser(is);
            user.setID(idNum.incrementAndGet());
            usersDB.put(user.getID(), user);
            System.out.println("Successfully created user " + user.getID());
            return Response.created(URI.create("/beers" + user.getID())).build();
        }

    @GET
    @Path("{id}")
    @Produces("application/JSON")
    public StreamingOutput getUser(@PathParam("id") int id){
        final User user = usersDB.get(id);

        if (user == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                outputUser(outputStream, user);
            }
        };
    }

    protected User readUser(InputStream is) throws IOException, ParseException {

        InputStreamReader read = new InputStreamReader(is);
        JSONParser parse = new JSONParser();
        JSONObject obj = (JSONObject)parse.parse(read);

        User user = new User();
        user.setUsername((String)obj.get("Username"));

        return user;
    }
    protected void outputUser(OutputStream os, User user){
        PrintStream writer = new PrintStream(os);

        JSONObject obj = new JSONObject();
        obj.put("Username", user.getUsername());
        writer.println(obj);

        JSONObject obj1 = new JSONObject();
        obj1.put("Favorites", user.getFavorites());
        writer.println(obj1);


    }


    }


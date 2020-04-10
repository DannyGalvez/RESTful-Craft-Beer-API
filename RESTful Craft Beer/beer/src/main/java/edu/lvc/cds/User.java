package edu.lvc.cds;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

// Generic class for a user object. Assigns variables for a specific user with getters/setters
// pre: None
// post: User is created with assigned variables
public class User {

    private String username;
    private ArrayList<String> favorites = new ArrayList<>(3);
    private int ID;
    private String URL;


    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public ArrayList<String> getFavorites(){
        return favorites;
    }
    public void addFavorite(String name){
        favorites.add(name);
    }
    public int getID(){
        return ID;
    }
    public void setID(int ID){
        this.ID = ID;
    }
    public void setURL(){
        URL = Main.BASE_URI + "users/";
    }
    public String getURL(){
        return URL;
    }

    public ArrayList<String> getFavoritesbyName(){
        BeerResource ab = new BeerResource();
        ConcurrentHashMap<Integer, Beer> db = ab.getBeersDB();
        ArrayList<String> favs = new ArrayList<>(3);
        for (int i = 0; i < favorites.size(); i++) {
            int ID = Integer.parseInt(favorites.get(i));
            favs.add(db.get(ID).getName());
        }
        return favs;
    }
    public void setFavorites(ArrayList<String> fav){
        favorites = fav;

    }

}

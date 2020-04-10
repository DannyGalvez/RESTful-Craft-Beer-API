package edu.lvc.cds;

import org.json.simple.JSONArray;

import java.util.ArrayList;

// Generic class for a beer object. Assigns variables for a specific beer with getters/setters
// pre: None
// post: Beer is created with assigned variables
public class Beer {

    private String name;
    private String brewery;
    private String ibu;
    private String ABV;
    private String rating;
    private String desc;
    private int ID;
    private String URL;

    private ArrayList<Double> ratings = new ArrayList<>(3);

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getBrewery(){
        return brewery;
    }
    public void setBrewery(String brewery){
        this.brewery = brewery;
    }
    public String getIbu(){
        return ibu;
    }
    public void setIbu(String ibu){
        this.ibu = ibu;
    }
    public String getABV(){
        return ABV;
    }
    public void setABV(String ABV){
        this.ABV = ABV;
    }
    public String getRating(){
        return rating;
    }
    public void setRating(String rating){
        ratings.add(Double.parseDouble(rating));
        this.rating = rating;
    }
    public String getDesc(){
        return desc;
    }
    public void setDesc(String desc){
        this.desc = desc;
    }
    public int getID(){
        return ID;
    }
    public void setID(int ID){
        this.ID = ID;
    }

    public void insertRating(String r){
        ratings.add(Double.parseDouble(r));
        calculateRating();
    }

    public ArrayList<Double> getAllRatings(){
        return ratings;
    }

    public void calculateRating(){
        double total = 0;
        for (int i = 0; i < ratings.size(); i++) {
            total+= (ratings.get(i));
        }
        this.rating = (String.valueOf(total/(ratings.size())));
    }

    public void setURL(){
        URL = Main.BASE_URI + "beers/";
    }

    public String getURL(){
        return URL;
    }
    public void setAllRatings(ArrayList<Double> rate){

        ratings = rate;

    }
    public void printRatings(){
        for (int i = 0; i < ratings.size(); i++) {
            System.out.println(ratings.get(i));
        }
    }




}

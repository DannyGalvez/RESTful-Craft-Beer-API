package edu.lvc.cds;

import org.json.simple.JSONArray;

import java.util.ArrayList;

public class Beer {

    private String name;
    private String brewery;
    private String ibu;
    private String ABV;
    private String rating;
    private String desc;
    private int ID;

    private ArrayList<Integer> ratings = new ArrayList<>(3);

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

    public void insertRating(int r){
        ratings.add(r);
        calculateRating();
    }

    public ArrayList<Integer> getAllRatings(){
        return ratings;
    }

    private void calculateRating(){
        int total = 0;
        for (int i = 0; i < ratings.size(); i++) {
            total+= ratings.get(i);
        }
        setRating(String.valueOf(total/(ratings.size())));
    }




}
